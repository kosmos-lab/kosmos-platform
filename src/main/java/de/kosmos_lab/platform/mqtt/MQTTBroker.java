package de.kosmos_lab.platform.mqtt;

import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * embedded MQTT Broker
 */
public final class MQTTBroker implements CommandInterface, io.moquette.broker.security.IAuthenticator, io.moquette.broker.security.IAuthorizatorPolicy {
    private static final String pre = "kosmos/";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("MQTTBroker");
    private static final Pattern re_set = Pattern.compile("^" + pre + "(?<uuid>[\\w ]*)/set$");
    private static final Pattern re_state = Pattern.compile("^" + pre + "(?<uuid>[\\w ]*)/state$");
    private static final Pattern re_config = Pattern.compile("^" + pre + "(?<uuid>[\\w ]*)/config$");
    private static final Pattern re_location = Pattern.compile("^" + pre + "(?<uuid>[\\w ]*)/location$");
    private static final Pattern re_key_set = Pattern.compile("^" + pre + "(?<uuid>.*)/(?<key>.*)/set$");
    private static final Pattern re_key_state = Pattern.compile("^" + pre + "(?<uuid>.*)/(?<key>.*)/state$");

    private static IController controller;
    private static Server mqttBroker;
    private int port;
    public HashMap<Device, String> stateTopic = new HashMap<>();
    public HashMap<Device, String> setTopic = new HashMap<>();


    public MQTTBroker() {
        //will only be used by the regular broker to instantiate an instance for auth
        //does not need to do anything, uses a static fallback to controlller
    }

    @SuppressFBWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public MQTTBroker(IController c) throws IOException {
        controller = c;

        MemoryConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, "false");
        config.setProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, "de.kosmos_lab.kosmos.controller.mqtt.MQTTBroker");
        config.setProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "de.kosmos_lab.kosmos.controller.mqtt.MQTTBroker");
        this.port = controller.getConfig().getJSONObject("mqtt").getInt("port");
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(port));
        mqttBroker = new Server();
        List<? extends InterceptHandler> userHandlers = Collections.singletonList(new PublisherListener(this));
        mqttBroker.startServer(config, userHandlers);
        c.addCommandInterface(this);
    }

    public void addDeviceTopics(Device device, String setTopic, String stateTopic) {
        this.stateTopic.put(device, stateTopic);
        this.setTopic.put(device, setTopic);

    }

    @Override
    public boolean canRead(Topic topic, String user, String client) {
        logger.info("check if {} can read {} ",user, topic.toString());
        Matcher m = re_state.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                }

            } catch (DeviceNotFoundException ex) {

            }
            return false;
        }
        m = re_key_state.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                }
            } catch (DeviceNotFoundException ex) {

            }
            return false;
        }
        m = re_location.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                }

            } catch (DeviceNotFoundException ex) {

            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        //logger.info("check if can write {} {}",topic.toString(),user);
        if (topic.equals("device/locations")) {
            return true;
        }
        Matcher m = re_set.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));
                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                    logger.warn("User {} tried to write to topic {}", user, topic);
                }

            } catch (DeviceNotFoundException ex) {

            }
            return false;
        }
        m = re_key_set.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                    logger.warn("User {} tried to write to topic {}", user, topic);
                }

            } catch (DeviceNotFoundException ex) {

            }
            return false;
        }
        m = re_config.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                try {
                    if (d.canWrite(controller.getUser(user))) {
                        return true;
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                    logger.warn("User {} tried to write to topic {}", user, topic);

                }
                return false;

            } catch (DeviceNotFoundException ex) {

            }

        }
        m = re_location.matcher(topic.toString());
        if (m.matches()) {
            try {
                Device d = controller.getDevice(m.group("uuid"));

                    try {
                        if (d.canWrite(controller.getUser(user))) {
                            return true;
                        }
                    } catch (NoAccessToScope noAccessToScope) {
                        //noAccessToScope.printStackTrace();
                        logger.warn("User {} tried to write to topic {}", user, topic);

                    }


                return true;
            } catch (DeviceNotFoundException ex) {

            }
        }
        return false;
    }

    @Override
    public boolean checkValid(String clientid, String username, byte[] bytes) {
        String password = new String(bytes, StandardCharsets.UTF_8);
        //logger.warn("check valid {} {}",username,password);
        try {
            IUser u = controller.tryLogin(username, password);
            if (u != null) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return false;

    }

    @Override
    public void deviceAdded(@CheckForNull CommandInterface from, Device device, CommandSourceName source) {
        String topic = pre + device.getUniqueID() + "/config";
        JSONObject obj = new JSONObject();
        obj.put("schema", device.getSchema().getId());
        String setTopic = pre + device.getUniqueID() + "/set";
        String stateTopic = pre + device.getUniqueID() + "/state";
        addDeviceTopics(device, setTopic, stateTopic);
        obj.put("set_topic", setTopic);
        obj.put("state_topic", stateTopic);
        this.publish(topic, obj.toString());

    }

    @Override
    public void deviceRemoved(@CheckForNull CommandInterface from, Device device, CommandSourceName source) {
        String topic = pre + device.getUniqueID() + "/config";
        this.publish(topic, "");
    }

    @Override
    public void deviceUpdate(@CheckForNull CommandInterface from, Device device, String key, CommandSourceName source) {
        String topic = pre + device.getUniqueID() + "/state";
        this.publish(topic, device.toJSON().toString());
        if (key != null) {
            this.publish(pre + device.getUniqueID() + "/" + key + "/state", device.get(key).toString());
        }
    }

    private IController getController() {
        return controller;
    }

    @Override
    public String getSourceName() {
        return "MQTTBroker";
    }

    private void publish(String topic, String payload) {
        MqttPublishMessage message = MqttMessageBuilders.publish()
                .topicName(topic)
                .retained(false)
                .qos(MqttQoS.EXACTLY_ONCE)
                .payload(Unpooled.copiedBuffer(payload.getBytes(UTF_8)))
                .build();
        mqttBroker.internalPublish(message, "KosmoS");
    }

    @Override
    public void stop() {
        mqttBroker.stopServer();
    }

    @Override
    public String toString() {
        return "MQTTBroker";
    }

    static class PublisherListener extends AbstractInterceptHandler {

        private final MQTTBroker broker;

        public PublisherListener(MQTTBroker mqttBroker) {
            this.broker = mqttBroker;
        }

        @Override
        public String getID() {
            return "EmbeddedLauncherPublishListener";
        }

        public CommandSourceName getSource(InterceptPublishMessage msg) {
            return broker.getController().getSource("MQTT:" + msg.getUsername());
        }

        private IUser getUser(InterceptPublishMessage msg) {
            return controller.getUser(msg.getUsername());
        }

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            try {
                //final String decodedPayload = new String(, UTF_8);
                final String payload = msg.getPayload().toString(UTF_8);
                String topic = msg.getTopicName();

                logger.info("Received on topic: " + topic + " content: " + payload);
                if (topic.equals("device/locations")) {
                    try {
                        IUser user = getUser(msg);

                        //JSONObject json = new JSONObject();
                        for (Device device : controller.getAllDevices()) {
                            try {
                                if (device.canRead(user)) {
                                    Device.Location loc = device.getLocation();
                                    if (loc != null) {
                                        this.broker.publish("device/" + device.getUniqueID() + "/location", loc.toJSON().toString());

                                    }
                                }
                            } catch (NoAccessToScope e) {

                            }

                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                Matcher m = re_key_set.matcher(topic);
                if (m.matches()) {

                    String uuid = m.group("uuid");
                    String key = m.group("key");
                    Device device = controller.getDevice(uuid);
                    logger.info("matched set for {} {} {}", uuid, key, payload);
                    if (device != null) {
                        //device.set(m.group("key"), payload, true);
                        controller.updateFromSource(broker, getSource(msg), device, m.group("key"), payload);
                    }
                    return;
                }
                m = re_set.matcher(topic);
                if (m.matches()) {
                    String uuid = m.group("uuid");
                    Device device = controller.getDevice(uuid);
                    if (device != null) {
                        if (payload.startsWith("{")) {
                            device.updateFromJSON(this.broker, new JSONObject(payload), getSource(msg));
                        }
                    }
                    return;
                }
                m = re_config.matcher(topic);
                if (m.matches()) {
                    String uuid = m.group("uuid");
                    if (payload.startsWith("{")) {
                        JSONObject json = new JSONObject(payload);
                        json.put("uuid", uuid);
                        try {
                            controller.parseAddDevice(this.broker, json, getSource(msg), getUser(msg));
                        } catch (DeviceAlreadyExistsException e) {
                            e.printStackTrace();
                        } catch (ParameterNotFoundException e) {
                            e.printStackTrace();
                        } catch (SchemaNotFoundException e) {
                            e.printStackTrace();
                        } catch (ValidationException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                m = re_location.matcher(topic);
                if (m.matches()) {
                    String uuid = m.group("uuid");
                    if (payload.startsWith("{")) {
                        JSONObject json = new JSONObject(payload);
                        json.put("uuid", uuid);
                        try {
                            controller.setLocation(getUser(msg), json, getSource(msg));
                        } catch (ParameterNotFoundException e) {
                            e.printStackTrace();

                        } catch (ValidationException e) {
                            e.printStackTrace();
                        } catch (NoAccessToScope noAccessToScope) {
                            noAccessToScope.printStackTrace();
                        }
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public int getPort() {
        return this.port;
    }
}