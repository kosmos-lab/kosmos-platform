/**
 * docker run -it --rm --name ros2_container --net=host althack/ros2:foxy-base
 * <p>
 * ros2 topic pub -1 /kosmos/virtualrgb4/speed std_msgs/Int32 "data: 87" ros2 topic echo /kosmos/virtualrgb4/speed
 * <p>
 * <p>
 * ros2 topic pub -1 /kosmos/virtualrgb4/text std_msgs/String "data: test3"
 * <p>
 * ros2 topic pub -1 /kosmos/virtualrgb4/on std_msgs/Bool "data: true"
 */
package de.kosmos_lab.platform.ros2;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.utils.KosmoSHelper;
import org.apache.commons.lang3.SystemUtils;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import std_msgs.msg.dds.*;
import us.ihmc.communication.packets.Packet;
import us.ihmc.pubsub.DomainFactory.PubSubImplementation;
import us.ihmc.pubsub.TopicDataType;
import us.ihmc.pubsub.subscriber.Subscriber;
import us.ihmc.ros2.NewMessageListener;
import us.ihmc.ros2.ROS2Node;
import us.ihmc.ros2.ROS2Publisher;
import us.ihmc.ros2.ROS2Subscription;
import us.ihmc.util.PeriodicNonRealtimeThreadSchedulerFactory;
import us.ihmc.util.PeriodicRealtimeThreadSchedulerFactory;
import us.ihmc.util.PeriodicThreadSchedulerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.String;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ROS2Controller implements CommandInterface {
    protected static final Logger logger = LoggerFactory.getLogger("Ros2Controller");
    private final IController controller;
    private final String pre;
    private final ROS2Controller me;
    private final ConcurrentHashMap<String, ROS2Publisher> publishers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ROS2Subscription> subscriptions = new ConcurrentHashMap<>();
    ROS2Node node = null;

    public ROS2Controller(IController controller) {
        this.me = this;
        this.pre = "kosmos";
        this.controller = controller;
        logger.info("booting ROS2Controller");
        if (KosmoSHelper.getEnvBool("USE_ROS2") || controller.getConfig().has("ros2")) {
            PeriodicThreadSchedulerFactory threadFactory = SystemUtils.IS_OS_LINUX ? // realtime threads only work on linux
                    new PeriodicRealtimeThreadSchedulerFactory(20) :           // see https://github.com/ihmcrobotics/ihmc-realtime
                    new PeriodicNonRealtimeThreadSchedulerFactory();                   // to setup realtime threads
            try {
                this.node = new ROS2Node(PubSubImplementation.FAST_RTPS, "KosmoS_Platform");
                //RealtimeROS2Publisher<Int64> publisher = node.createPublisher(new Int64PubSubType(), "/example", ROS2QosProfile.KEEP_HISTORY(3), 10);
                //RealtimeROS2Subscription<Int64> subscription = node.createQueuedSubscription(new Int64PubSubType(), "/example", ROS2QosProfile.KEEP_HISTORY(3), 10);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //node.spin(); // start the realtime node thread
        }
    }

    public TopicDataType<? extends Packet<? extends Packet<?>>> getType(Schema schema) {
        if (schema instanceof ObjectSchema) {

        } else {
            if (schema instanceof NumberSchema) {
                if (((NumberSchema) schema).requiresInteger()) {

                    return new Int32PubSubType();

                } else {
                    if (((NumberSchema) schema).requiresInteger()) {
                        return new Float64PubSubType();

                    }
                }
            }
            if (schema instanceof BooleanSchema) {
                return new BoolPubSubType();
            }
            if (schema instanceof StringSchema) {
                return new StringPubSubType();
            }
        }
        return null;
    }

    private void sendState(Device device, String uuid, JSONObject json, ObjectSchema schema, String pre_state) {
        logger.info("sendState {} {}", uuid, json);
        if (json == null) {
            return;

        }
        if (pre_state.startsWith("/")) {
            pre_state = pre_state.substring(1);
        }

        for (Map.Entry<String, Schema> entry : schema.getPropertySchemas().entrySet()) {
            Schema prop_schema = entry.getValue();
            String topic = String.format("/%s/%s/%s%s", pre, uuid.replace(".", "_").replace("-", "_"), pre_state, entry.getKey());
            ROS2Publisher publisher = publishers.get(topic);
            if (prop_schema instanceof ReferenceSchema) {
                prop_schema = ((ReferenceSchema) prop_schema).getReferredSchema();
            }
            //logger.info("topic {} {} {}", topic, entry.getValue().getClass(), entry.getValue(), (publisher == null));


            TopicDataType type = getType(prop_schema);

            if (publisher == null) {
                if (prop_schema instanceof ObjectSchema) {
                    String t = String.format("%s/%s/", pre_state, entry.getKey());
                    sendState(device, uuid, json.optJSONObject(entry.getKey()), (ObjectSchema) prop_schema, t);
                } else {
                    if (type != null) {

                        try {
                            publisher = node.createPublisher(type, topic);
                            this.publishers.put(topic, publisher);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
            if (publisher != null) {
                try {
                    Object v = json.opt(entry.getKey());
                    if (v != null) {
                        if (type instanceof Int64PubSubType) {
                            if (v instanceof Long || v instanceof Integer) {
                                Int64 i = new Int64();
                                i.setData((long) v);
                                v = i;
                            }

                        } else if (type instanceof Int32PubSubType) {
                            if (v instanceof Long || v instanceof Integer) {
                                Int32 i = new Int32();
                                i.setData((Integer) v);
                                v = i;
                            }

                        } else if (type instanceof Float32PubSubType) {
                            if (v instanceof Double || v instanceof Float || v instanceof Long || v instanceof Integer) {
                                Float32 i = new Float32();
                                i.setData((Float) v);
                                v = i;
                            }

                        } else if (type instanceof Float64PubSubType) {
                            if (v instanceof Double || v instanceof Float || v instanceof Integer || v instanceof Long) {
                                Float64 i = new Float64();
                                i.setData((Double) v);
                                v = i;
                            }

                        } else if (type instanceof BoolPubSubType) {
                            if (v instanceof Boolean) {
                                if ((Boolean) v) {
                                    v = new Bool();
                                    ((Bool) v).setData(true);
                                } else {
                                    v = new Bool();
                                    ((Bool) v).setData(false);
                                }
                            }

                        } else if (type instanceof StringPubSubType) {
                            std_msgs.msg.dds.String t = new std_msgs.msg.dds.String();
                            t.setData(v.toString());
                            v = t;
                        }
                        logger.info("publishing to {} : {}", topic, v);
                        publisher.publish(v);
                    }
                } catch (Exception e) {
                    logger.error("Exception ", e);
                }
                if (schema.isReadOnly() == null || !schema.isReadOnly()) {
                    if (type != null) {
                        ROS2Subscription subscriber = subscriptions.get(topic);
                        if (subscriber == null) {
                            try {
                                logger.info("creating topic {} pre_state {}", topic, pre_state);
                                subscriber = node.createSubscription(type, new KosmosNewMessageListener<Object>(device, pre_state) {
                                    @Override
                                    public void onNewDataMessage(Subscriber<Object> subscriber) {

                                        Object data = subscriber.readNextData();
                                        logger.info("pre_State was: {}", this.pre_state);


                                        if (data instanceof Int32) {
                                            //logger.info("source {}", ((Int32) data).getSource());

                                            data = ((Int32) data).data_;
                                        } else if (data instanceof Int64) {
                                            //logger.info("source {}", ((Int64) data).getSource());
                                            data = ((Int64) data).data_;
                                        } else if (data instanceof Float64) {
                                            //logger.info("source {}", ((Float64) data).getSource());
                                            data = ((Float64) data).data_;
                                        } else if (data instanceof Float32) {
                                            //logger.info("source {}", ((Float32) data).getSource());
                                            data = ((Float32) data).data_;
                                        } else if (data instanceof Bool) {
                                            //logger.info("source {}", ((Bool) data).getSource());
                                            data = ((Bool) data).data_;
                                        } else if (data instanceof std_msgs.msg.dds.String) {
                                            //logger.info("source {}", ((std_msgs.msg.dds.String) data).getSource());
                                            data = ((std_msgs.msg.dds.String) data).data_.toString();
                                        }
                                        if (pre_state.length() > 0) {
                                            JSONObject patch = new JSONObject();
                                            //{"op":"add","path":"/a","value"
                                            String path = String.format("%s%s", this.pre_state, entry.getKey());
                                            logger.info("path {}", path);
                                            patch.put("op", "add").put("path", path).put("value", data);

                                            //first try adding it with empty data besides the change
                                            JSONObject j = de.kosmos_lab.utils.JSONPatch.apply(new JSONObject(), new JSONArray().put(patch));

                                            //j.put(entry.getKey(), data);
                                            logger.info("new JSON: {}", j);
                                            try {
                                                controller.parseSet(me, this.device, j, controller.getSource(this.getClass().getName()), null);
                                            } catch (DeviceNotFoundException e) {
                                                throw new RuntimeException(e);
                                            } catch (NoAccessToScope e) {
                                                throw new RuntimeException(e);
                                            } catch (ValidationException e) {
                                                j = de.kosmos_lab.utils.JSONPatch.apply(this.device, new JSONArray().put(patch));
                                                try {
                                                    //if it fails - try to get the whole state and change only the needed one
                                                    controller.parseSet(me, this.device, j, controller.getSource(this.getClass().getName()), null);
                                                } catch (DeviceNotFoundException e2) {
                                                    throw new RuntimeException(e2);
                                                } catch (NoAccessToScope e2) {
                                                    throw new RuntimeException(e2);
                                                }
                                            }
                                        } else {
                                            //try setting this value only first
                                            JSONObject j = new JSONObject();
                                            j.put(entry.getKey(), data);
                                            try {
                                                controller.parseSet(me, this.device, j, controller.getSource(this.getClass().getName()), null);
                                            } catch (DeviceNotFoundException e) {
                                                throw new RuntimeException(e);
                                            } catch (NoAccessToScope e) {
                                                throw new RuntimeException(e);
                                            } catch (ValidationException e) {
                                                //if the validation fails, lets try to get the complete state and update this entry, useful for stuff like color array, which always needs r/g/b to be filled.
                                                j = new JSONObject(this.device.toMap());
                                                j.put(entry.getKey(), data);
                                                try {
                                                    controller.parseSet(me, this.device, j, controller.getSource(this.getClass().getName()), null);
                                                } catch (DeviceNotFoundException ex) {
                                                    throw new RuntimeException(ex);
                                                } catch (NoAccessToScope ex) {
                                                    throw new RuntimeException(ex);
                                                }

                                            }
                                        }

                                    }
                                }, topic);
                                subscriptions.put(topic, subscriber);

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            }

        }


    }

    @Override
    public void deviceAdded(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {
        if (node != null) {
            sendState(device, device.getUniqueID(), device, device.getSchema(), "");
        }

    }

    @Override
    public void deviceRemoved(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {

    }

    @Override
    public void deviceUpdate(@Nullable CommandInterface from, @Nonnull Device device, @Nullable String key, @Nonnull CommandSourceName source) {
        if (node != null) {
            if (key == null) {
                sendState(device, device.getUniqueID(), device, device.getSchema(), "");
            } else {
                JSONObject json = new JSONObject();
                json.put(key, device.get(key));
                sendState(device, device.getUniqueID(), json, device.getSchema(), "");
            }
        }
    }

    @Override
    public void stop() {
        if (node != null) {
            node.destroy();
        }
    }

    @Override
    public String getSourceName() {
        return "ROS2";
    }

    public abstract class KosmosNewMessageListener<T> implements NewMessageListener<T> {

        public final Device device;
        public final String pre_state;

        KosmosNewMessageListener(Device device, String pre_state) {
            this.device = device;
            this.pre_state = pre_state;
        }

    }
}
