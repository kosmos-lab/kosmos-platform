/**
 * docker run -it --rm --name ros2_container --net=host althack/ros2:foxy-base
 *
 * ros2 topic pub -1 /kosmos/virtualrgb4/speed std_msgs/Int32 "data: 87"
 * ros2 topic echo /kosmos/virtualrgb4/speed
 *
 *
 *  ros2 topic pub -1 /kosmos/virtualrgb4/text std_msgs/String "data: test3"
 *
 *   ros2 topic pub -1 /kosmos/virtualrgb4/on std_msgs/Bool "data: true"
 *
 *
 */
package de.kosmos_lab.platform.ros2;

import de.kosmos_lab.platform.KosmoSController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import org.apache.commons.lang3.SystemUtils;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rcl_interfaces.msg.dds.IntegerRangePubSubType;
import std_msgs.msg.dds.*;
import us.ihmc.communication.packets.Packet;
import us.ihmc.pubsub.DomainFactory.PubSubImplementation;
import us.ihmc.pubsub.TopicDataType;
import us.ihmc.pubsub.subscriber.Subscriber;
import us.ihmc.ros2.NewMessageListener;
import us.ihmc.ros2.QueuedROS2Subscription;
import us.ihmc.ros2.ROS2Node;
import us.ihmc.ros2.ROS2Publisher;
import us.ihmc.ros2.ROS2QosProfile;
import us.ihmc.ros2.ROS2Subscription;
import us.ihmc.ros2.RealtimeROS2Node;
import us.ihmc.ros2.RealtimeROS2Publisher;
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
    private final KosmoSController controller;
    private final String pre;
    private final ROS2Controller me;
    ROS2Node node = null;
    private final ConcurrentHashMap<String, ROS2Publisher> publishers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ROS2Subscription> subscriptions = new ConcurrentHashMap<>();

    public ROS2Controller(KosmoSController kosmoSController, String pre) {
        logger.info("starting ROS2Controller");
        this.me = this;
        this.pre = pre;
        this.controller = kosmoSController;
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
            if ( schema instanceof BooleanSchema) {
                return new BoolPubSubType();
            }
            if ( schema instanceof StringSchema) {
                return new StringPubSubType();
            }
        }
        return null;
    }
    protected static final Logger logger = LoggerFactory.getLogger("Ros2Controller");

    private void sendState(String uuid, JSONObject json, ObjectSchema schema,String pre_state) {
        logger.info("sendState {} {}", uuid, json);

        for (Map.Entry<String, Schema> entry : schema.getPropertySchemas().entrySet()) {
            Schema prop_schema = entry.getValue();
            String topic = String.format("/%s/%s/%s%s", pre, uuid.replace(".","_").replace("-","_"), pre_state, entry.getKey());
            ROS2Publisher publisher = publishers.get(topic);


            TopicDataType type = getType(prop_schema);

            if (publisher == null) {
                if (prop_schema instanceof ObjectSchema) {
                    sendState(uuid, json.optJSONObject(entry.getKey()), (ObjectSchema) prop_schema, String.format("%s_%s", pre_state, entry.getKey()));
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
                    ROS2Subscription subscriber = subscriptions.get(topic);
                    if (subscriber == null) {
                        try {
                            subscriber = node.createSubscription(type, new NewMessageListener<Object>() {

                                @Override
                                public void onNewDataMessage(Subscriber<Object> subscriber) {

                                    Object data = subscriber.readNextData();
                                    if (data instanceof Int32) {
                                        data = ((Int32) data).data_;
                                    } else if (data instanceof Int64) {
                                        data = ((Int64) data).data_;
                                    } else if (data instanceof Float64) {
                                        data = ((Float64) data).data_;
                                    } else if (data instanceof Float32) {
                                        data = ((Float32) data).data_;
                                    } else if (data instanceof Bool) {
                                        data = ((Bool) data).data_;
                                    } else if (data instanceof std_msgs.msg.dds.String) {
                                        data = ((std_msgs.msg.dds.String) data).data_.toString();
                                    }
                                    JSONObject j = new JSONObject();
                                    j.put(entry.getKey(), data);

                                    try {
                                        controller.parseSet(me, uuid, j, controller.getSource(this.getClass().getName()), null);
                                    } catch (DeviceNotFoundException e) {
                                        throw new RuntimeException(e);
                                    } catch (NoAccessToScope e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }, topic);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }
            }

        }


    }

    @Override
    public void deviceAdded(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {
sendState(device.getUniqueID(),device,device.getSchema(),"");

    }

    @Override
    public void deviceRemoved(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {

    }

    @Override
    public void deviceUpdate(@Nullable CommandInterface from, @Nonnull Device device, @Nullable String key, @Nonnull CommandSourceName source) {
        sendState(device.getUniqueID(),device,device.getSchema(),"");

    }

    @Override
    public void stop() {

    }

    @Override
    public String getSourceName() {
        return null;
    }
}
