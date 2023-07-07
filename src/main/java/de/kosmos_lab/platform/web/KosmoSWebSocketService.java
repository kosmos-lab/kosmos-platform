package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Event;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.platform.persistence.Constants;
import de.kosmos_lab.platform.rules.RulesService;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.smarthome.EventInterface;
import de.kosmos_lab.platform.web.servlets.user.AuthServlet;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.servers.AsyncServer;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.Channel;
import de.kosmos_lab.web.doc.openapi.Message;
import de.kosmos_lab.web.doc.openapi.WebSocketEndpoint;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.server.JWT;
import de.kosmos_lab.web.server.WebSocketService;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;

@Schema(
        name = "user_token",
        type = SchemaType.STRING,
        allowableValues = "user/token:{token}"

)
@AsyncServer(
        protocol = "wss",
        name = "wss",
        url = "wss://${host}",
        description = "Websocket base.\n\n<b><em>INFO</em></b>:All messages tagged with \"authRequired\" in the binding need to have the client authenticate, this can be done either via <a href=\"#operation-subscribe-user/login\">user/login</a> or <a href=\"#operation-subscribe-user/token\">user/token</a>"
)
@AsyncServer(
        protocol = "mqtt",
        name = "mqtt",
        url = "mqtt://${host}"
)


/**
 * This is the Service used to control the KosmoS Websocket on /ws
 */
@WebSocketEndpoint(
        path = "/ws",
        enableMQTT = true,
        enableWS = true,
        channels = {
                @Channel(
                        tags = {@Tag(name = "KosmoS")},
                        path = "user/token",
                        subscribeMessages = {
                                @Message(
                                        name = "user/token",
                                        payloadSchema = {@Schema(ref =
                                                "#/components/schemas/user_token"
                                        )}
                                )
                        }
                ),
                @Channel(
                        tags = {@Tag(name = "KosmoS")},
                        path = "user/login",
                        subscribeMessages = {
                                @Message(
                                        name = "user/login",
                                        payload = @ObjectSchema(
                                                properties = {
                                                        @SchemaProperty(
                                                                name = AuthServlet.FIELD_USER,
                                                                schema = @Schema(type = SchemaType.STRING, description = "the username to login in with")
                                                        ),
                                                        @SchemaProperty(
                                                                name = AuthServlet.FIELD_PASS,
                                                                schema = @Schema(type = SchemaType.STRING, description = "the password to login in with")
                                                        )
                                                },
                                                examples = {
                                                        @ExampleObject(name = "login for karl", value = "{\"" + AuthServlet.FIELD_USER + "\":\"karl\",\"" + AuthServlet.FIELD_PASS + "\",\"verysecret\"}")
                                                }
                                        )
                                )
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "locations",
                        needsMessage = false,
                        subscribeMessages = {
                                @Message(
                                        description = "This messages signals to the server, that we are interested in getting all device locations. The server will return all locations via <a href=\"#operation-publish-device/{uuid}/location\">device/{uuid}/location</a>.",
                                        //xResponseRefs = "#/components/messages/deviceLocation",
                                        payloadSchema = @Schema()
                                )
                        }

                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/set",

                        subscribeMessages = {
                                @Message(
                                        name = "device_set",
                                        payloadSchema = @Schema(type = SchemaType.OBJECT, description = "the value(s) to set",
                                                examples = {
                                                        @ExampleObject(name = "change temperature for heating", value = "{\"heatingTemperatureSetting\":20.5}"),
                                                        @ExampleObject(name = "set rgb color", value = "{\"color\":{\"r\":255,\"g\":0,\"b\":255}}"),
                                                        @ExampleObject(name = "turn off ", value = "{\"on\":false}"),
                                                        @ExampleObject(name = "turn off HomeAssistant device", value = "{\"state\":\"OFF\"}")
                                                }
                                        )
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "the uuid of the device you want to control",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/event",

                        subscribeMessages = {
                                @Message(
                                        name = "device_event",
                                        payloadSchema = @Schema(ref = "/doc/openapi.yaml#/components/schemas/event")
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "The UUID of the device this event belongs to, events will only be visible for users with read access to the device",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "/event",

                        subscribeMessages = {
                                @Message(
                                        name = "event",

                                        payloadSchema = @Schema(ref =
                                                "/doc/openapi.yaml#/components/schemas/event"
                                        )
                                )
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/location",

                        subscribeMessages = {
                                @Message(
                                        name = "device/setLocation",

                                        payloadSchema = @Schema(ref =
                                                "/doc/openapi.yaml#/components/schemas/deviceLocation"
                                        )
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "the uuid of the device you want to control",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/config",

                        subscribeMessages = {
                                @Message(
                                        name = "device/add",
                                        payload = @ObjectSchema(
                                                properties = {
                                                        @SchemaProperty(
                                                                name = "schema",
                                                                schema = @Schema(

                                                                        description = "The $id/url of the schema to use. If its a schema not already in the system the $id MUST be a reachable Url describing the schema.",
                                                                        type = SchemaType.STRING,
                                                                        required = true
                                                                )
                                                        ),

                                                        @SchemaProperty(
                                                                name = "name",
                                                                schema = @Schema(

                                                                        description = "The name to use for the new device, if no name is set uuid will be used. Does not need to be unique.",
                                                                        type = SchemaType.STRING,
                                                                        minLength = 3,
                                                                        required = false
                                                                )
                                                        ),
                                                        @SchemaProperty(
                                                                name = "state",
                                                                schema = @Schema(

                                                                        description = "The starting state of the device, needs to contain all required values if the schema has any and needs to be valid against the schema.",
                                                                        type = SchemaType.OBJECT,
                                                                        defaultValue = "{}",
                                                                        required = false
                                                                )
                                                        ),
                                                        @SchemaProperty(
                                                                name = "scopes",
                                                                schema = @Schema(
                                                                        description = "The name of the scope to use for the new device.",
                                                                        ref = "/doc/openapi.yaml#/components/schemas/deviceScopes"
                                                                )
                                                        ),
                                                }, examples = {
                                                @ExampleObject(
                                                        name = "/device/multi2/config",
                                                        value = "{\"name\":\"multi2\",\"schema\":\"https://kosmos-lab.de/schema/MultiSensor.json\",\"state\":{\"currentEnvironmentTemperature\":17,\"humidityLevel\":10}}"
                                                ),
                                                @ExampleObject(
                                                        name = "/device/kosmos_multi17/config",
                                                        value = "{\"name\":\"kosmos_multi17\",\"schema\":\"https://kosmos-lab.de/schema/MultiSensor.json\",\"state\":{\"currentEnvironmentTemperature\":17,\"humidityLevel\":10},\"scopes\":{\"read\":\"kosmos:read\",\"write\":\"kosmos:write\",\"del\":\"kosmos:del\"}}"
                                                ),
                                                @ExampleObject(
                                                        name = "/device/lamp1/config",
                                                        value = "{\"name\":\"lamp1\",\"schema\":\"https://kosmos-lab.de/schema/Lamp.json\",\"state\":{\"on\":true}}"
                                                )}
                                        )
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "the uuid of the device you want to control",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/state",
                        publishMessages = {
                                @Message(
                                        name = "device/state",
                                        payloadSchema = @Schema(type = SchemaType.OBJECT, description = "the state of the given device",
                                                examples = {
                                                        @ExampleObject(name = "changed temperature for heating", value = "{\"heatingTemperatureSetting\":20.5}"),
                                                        @ExampleObject(name = "updated rgb color", value = "{\"color\":{\"r\":255,\"g\":0,\"b\":255}}"),
                                                        @ExampleObject(name = "turned off ", value = "{\"on\":false}"),
                                                        @ExampleObject(name = "turned off HomeAssistant device", value = "{\"state\":\"OFF\"}")
                                                }
                                        )
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "the uuid of the device",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                ),
                @Channel(
                        userLevel = 1,
                        tags = {@Tag(name = "KosmoS")},
                        path = "device/{uuid}/location",
                        publishMessages = {
                                @Message(
                                        name = "device/location",
                                        payloadSchema = @Schema(ref =
                                                "/doc/openapi.yaml#/components/schemas/deviceLocation"
                                        )
                                )
                        },
                        parameters = {
                                @Parameter(
                                        description = "the uuid of the device the location refers to",
                                        name = "uuid",
                                        schema = @Schema(type = SchemaType.STRING),
                                        in = ParameterIn.PATH)
                        }
                )
        }
)
@WebSocket
public class KosmoSWebSocketService extends WebSocketService implements CommandInterface, EventInterface {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSWebSocketService");
    private final IController controller;
    private final ConcurrentHashMap<Session, IUser> logins = new ConcurrentHashMap<Session, IUser>();
    private final ConcurrentHashMap<Session, String> types = new ConcurrentHashMap<Session, String>();
    private final Pinger pinger;
    public ConcurrentHashMap<String, MessageTimer> messageTimers = new ConcurrentHashMap<>();
    ConcurrentHashMap<IUser, HashSet<Device>> ignoredDevices = new ConcurrentHashMap<>();


    public KosmoSWebSocketService(KosmoSWebServer server, IController c) {
        super(server);
        this.controller = c;
        this.pinger = new Pinger(this);
        this.pinger.start();
        controller.addCommandInterface(this);
        controller.addEventInterface(this);
    }

    private CommandSourceName getSourceName(Session session) {


        return controller.getSource("WebSocket" + session.getLocalAddress().toString());

    }

    private CommandSourceName getSourceName(Session session, String pre) {


        return controller.getSource(pre + "WebSocket" + session.getLocalAddress().toString());

    }

    public void broadCast(String text) {
        broadCast(text, null);
    }

    public void broadCast(String text, Session sess) {
        synchronized (logins) {

            for (Map.Entry<Session, IUser> e : this.logins.entrySet()) {
                if (e.getValue() != null) {
                    if (sess != e.getKey()) {
                        try {
                            e.getKey().getRemote().sendString(text);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void broadcastToReadUsers(Device device, String text, CommandSourceName source) {
        broadcastToReadUsers(device, text, source, null);

    }

    public void broadcastToReadUsers(Device device, String text, CommandSourceName source, Session sourceSession) {
        synchronized (logins) {

            for (Map.Entry<Session, IUser> e : this.logins.entrySet()) {
                try {
                    IUser user = e.getValue();
                /*if ( source != null && source == getSourceName(e.getKey())) {
                    continue;
                }*/
                    if (user != null && device.canRead(user)) {
                        try {
                            if (source != null && source.getSourceName().startsWith("haset")) {
                                String type = this.types.get(e.getKey());

                                if ("HAIntegration".equals(type) || user.getName().equalsIgnoreCase("ha")) {
                                    logger.trace("SKIPPING broadcast, because it came from HASET in the first place");
                                    continue;
                                }
                            }
                            if (sourceSession != null && sourceSession == e.getKey()) {
                                continue;
                            }
                            e.getKey().getRemote().sendString(text);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (NoAccessToScope noAccessToScope) {
                    //noAccessToScope.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deviceAdded(CommandInterface from, Device device, CommandSourceName source) {
        broadcastToReadUsers(device, "device/" + device.getUniqueID() + "/config:" + device.toJSON(), source);

    }

    @Override
    public void deviceRemoved(CommandInterface from, Device device, CommandSourceName source) {
        broadcastToReadUsers(device, "device/" + device.getUniqueID() + "/config:", source);

    }

    @Override
    public void deviceUpdate(CommandInterface from, Device device, String key, CommandSourceName source) {

        broadcastToReadUsers(device, "device/" + device.getUniqueID() + "/state:" + device, source);

    }

    @Override
    public void eventFired(@Nullable EventInterface from, @Nonnull Event event) {
        eventFired(from, event, null);
    }

    public void eventFired(@Nullable EventInterface from, @Nonnull Event event, Session sourceSession) {

        if (from != this) {
            if (event.getDevice() != null) {

                this.broadcastToReadUsers(event.getDevice(), String.format("device/%s/event:%s", event.getDevice().getUniqueID(), event.toJSON().toString()), null, sourceSession);
            } else {
                this.broadCast(String.format("event:%s", event.toJSON().toString()), sourceSession);
            }
        }
    }

    @Override
    public void stop() {


        for (Session s : this.sessions) {
            try {
                s.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    @Override
    public void delWebSocketClient(Session sess) {
        super.delWebSocketClient(sess);
        this.logins.remove(sess);
    }

    @OnWebSocketMessage
    public void onWebSocketMessage(Session sess, String message) {

        IUser user = logins.get(sess);
        if (user != null) {
            String type = types.get(sess);
            if (type != null) {
                logger.info("Received TEXT message: {} from: {} {} ({}) ", message, sess, user.getName(), type);
            } else {
                logger.info("Received TEXT message: {} from: {} {} ", message, sess, user.getName());
            }
        } else {
            logger.info("Received TEXT message: {} from {}", message, sess);
        }

        if (message.equalsIgnoreCase("ping")) {
            try {
                sess.getRemote().sendString("pong");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Matcher m = Constants.websocketSplitPattern.matcher(message);
        if (m.matches()) {
            String topic = m.group(1);
            if (topic == null) {
                return;
            }

            if (topic.startsWith("kosmos/")) {
                topic = topic.substring(7);
            }
            if (topic.startsWith("device/")) {
                topic = topic.substring(7);
            }
            String payload = m.group(2);
            //logger.info("topic {}", topic);
            if (topic.equals("user/auth") || topic.equals("user/login")) {
                JSONObject json = new JSONObject(payload);
                String usern = json.optString("user");
                String pass = json.optString("pass");
                if (usern != null && pass != null) {
                    IUser u = null;
                    try {
                        u = controller.tryLogin(usern, pass);
                        if (u != null) {
                            logins.put(sess, u);
                            logger.info("auth successful");
                            afterAuth(sess, u);
                            return;
                        }
                    } catch (LoginFailedException e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    logger.warn("json not correct");
                }
                try {
                    logger.warn("json auth failed!");
                    sess.getRemote().sendString("auth failed");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (topic.equals("user/token")) {
                if (payload != null && !payload.equals("null")) {


                    try {
                        JSONObject o = this.controller.getJwt().verify(payload);
                        IUser u = this.controller.getUser(o.getInt("id"));

                        logins.put(sess, u);
                        logger.info("auth successful");

                        sess.getRemote().sendString("auth successful");
                        afterAuth(sess, u);


                        return;


                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (JWT.JWTVerifyFailed e) {
                        e.printStackTrace();
                    } catch (UserNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    logger.info("token auth failed!");

                    sess.getRemote().sendString("auth failed");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (topic.equals("user/type")) {
                types.put(sess, payload);
            } else {

                if (user != null) {

                    if (topic.equals("kree/stdout")) {
                        try {

                            JSONObject obj = new JSONObject();
                            obj.put("type", "log");
                            obj.put("value", payload);
                            RulesService s = controller.getRulesService();
                            if (s != null) {
                                s.broadcastToUser(logins.get(sess), obj.toString());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                        return;
                    }
                    if (topic.equals("kree/stderr")) {
                        try {

                            JSONObject obj = new JSONObject();
                            obj.put("type", "error");
                            obj.put("value", payload);

                            RulesService s = controller.getRulesService();
                            if (s != null) {
                                s.broadcastToUser(logins.get(sess), obj.toString());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                    if (topic.equals("locations")) {
                        try {

                            //JSONObject json = new JSONObject();
                            for (Device device : this.controller.getAllDevices()) {
                                try {
                                    if (device.canRead(user)) {
                                        Device.Location loc = device.getLocation();
                                        if (loc != null) {
                                            sess.getRemote().sendString("device/" + device.getUniqueID() + "/location:" + loc.toJSON().toString());
                                        }
                                    }
                                } catch (NoAccessToScope e) {

                                }

                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                        return;
                    }
                    if (topic.endsWith("/set")) {
                        String uuid = topic.substring(0, topic.length() - 4);
                        try {
                            JSONObject pl = new JSONObject();
                            try {
                                pl = new JSONObject(payload);
                            } catch (JSONException ex) {

                            }
                            String type = types.get(sess);
                            if (type != null) {
                                if (type.equals("HAIntegration")) {
                                    controller.parseHASet(this, uuid, pl, getSourceName(sess, "haset/"), user);
                                    return;
                                }
                            }

                            controller.parseSet(this, uuid, pl, getSourceName(sess), user);
                        } catch (DeviceNotFoundException | NoAccessToScope e) {
                            logger.warn(e.getMessage());
                            //e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (topic.endsWith("/haset")) {
                        MessageTimer mt = this.messageTimers.get(topic);
                        if (mt == null) {
                            mt = new MessageTimer();
                            this.messageTimers.put(topic, mt);
                        }
                        int c = mt.count();
                        //logger.info("mt.count {}", c);
                        if (c > 3) {
                            return;
                        }
                        mt.addEntry();
                        String uuid = topic.substring(0, topic.length() - 6);
                        try {
                            //logger.info("payload {}",payload);
                            JSONObject p = new JSONObject(payload);
                            if (!p.has("state")) {
                                p.put("state", new JSONObject());
                            }
                            controller.parseHASet(this, uuid, p, getSourceName(sess, "haset/"), user);
                        } catch (DeviceNotFoundException | NoAccessToScope ex) {
                            logger.warn("Exception:", ex);
                        } catch (org.json.JSONException ex) {
                            logger.warn("Exception:", ex);

                        } catch (Exception ex) {
                            logger.warn("Exception:", ex);
                        }
                    } else if (topic.endsWith("/setname")) {
                        String uuid = topic.substring(0, topic.length() - 8);

                        Device d = null;
                        try {
                            d = controller.getDevice(uuid);
                            controller.setName(d, payload);
                        } catch (DeviceNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else if (topic.endsWith("/config")) {
                        //String uuid = topic.substring(0, topic.length() - 7);
                        try {
                            if (payload.length() > 2) {
                                controller.parseAddDevice(this, new JSONObject(payload), getSourceName(sess), user);
                            } else {
                                String uuid = topic.substring(0, topic.length() - 7);
                                Device d = controller.getDevice(uuid);
                                if (d != null) {
                                    try {
                                        if (d.canDel(user)) {
                                            controller.deleteDevice(this, d);
                                        }
                                    } catch (NoAccessToScope e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        } catch (DeviceAlreadyExistsException e) {
                            //e.printStackTrace();
                        } catch (ParameterNotFoundException | SchemaNotFoundException e) {
                            logger.warn(e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (topic.endsWith("/location")) {
                        //
                        try {
                            JSONObject json = new JSONObject(payload);
                            //json.put("uuid",uuid);
                            if (!json.has("uuid")) {
                                json.put("uuid", topic.substring(0, topic.length() - 9));
                            }
                            controller.setLocation(user, json, getSourceName(sess));

                        } catch (DeviceNotFoundException e) {
                            //e.printStackTrace();
                        } catch (ParameterNotFoundException e) {
                            logger.warn(e.getMessage());
                        } catch (NoAccessToScope noAccessToScope) {
                            noAccessToScope.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (topic.equals("event")) {
                        //
                        try {
                            JSONObject json = new JSONObject(payload);
                            //json.put("uuid",uuid);
                            Event event = new Event(controller, this, json, null);
                            this.controller.fireEvent(event, this);
                            this.eventFired(this, event, sess);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (topic.endsWith("/event")) {
                        //
                        try {
                            JSONObject json = new JSONObject(payload);
                            //json.put("uuid",uuid);

                            Device d = this.controller.getDevice(topic.substring(0, topic.length() - 6));
                            if (d.canRead(user)) {

                                Event event = new Event(controller, this, json, d);
                                this.controller.fireEvent(event, this);
                                this.eventFired(this, event, sess);
                            }

                        } catch (DeviceNotFoundException e) {
                            //e.printStackTrace();
                        } catch (NoAccessToScope noAccessToScope) {
                            noAccessToScope.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    logger.warn("user is NOT authed!");
                }
            }
        }
        //broadCast(message, sess);
    }

    public void addIgnoredDevice(IUser user, Device device) {
        HashSet<Device> set = this.ignoredDevices.get(user);
        if (set == null) {
            set = new HashSet<>();
            this.ignoredDevices.put(user, set);
        }
        set.add(device);
    }

    private void afterAuth(Session sess, IUser u) {
        try {
            sess.getRemote().sendString("auth successful");
            HashSet<DataSchema> schemas = new HashSet<>();

            JSONArray arr = new JSONArray();
            HashSet<Device> set = this.ignoredDevices.get(u);

            for (Device device : this.controller.getAllDevices()) {
                try {


                    if (device.canRead(u)) {
                        schemas.add(device.getDataSchema());
                        JSONObject d = device.toJSON();
                        if (device.canWriteReadOnly(u)) {
                            d.put("canWriteReadOnly", true);
                        }
                        if (set != null && set.contains(device)) {
                            if (d.has("state")) {
                                d.remove("state");
                            }
                        }
                        arr.put(d);
                    }
                } catch (NoAccessToScope e) {

                }

            }
            JSONObject s = new JSONObject();
            for (DataSchema schema : schemas) {
                s.put(schema.getSchema().getId(), schema.getRawSchema());
            }
            sess.getRemote().sendString("schemas:" + s);
            sess.getRemote().sendString("devices:" + arr);

        } catch (Exception e) {
            logger.warn("Exception:", e);
        }
    }

    @Override
    public String getSourceName() {
        return "HTTPApi";
    }

    @Override
    public void ping() {
        broadCast("ping");

    }

    private static class MessageTimer {
        public ConcurrentLinkedQueue<MessageTimerEntry> entries = new ConcurrentLinkedQueue<>();

        public void addEntry() {


            this.entries.add(new MessageTimerEntry());

        }

        public int count() {
            long now = System.currentTimeMillis();

            int c = 0;

            Iterator<MessageTimerEntry> it = entries.iterator();
            while (it.hasNext()) {
                MessageTimerEntry e = it.next();
                if (e.aliveUntil <= now) {
                    //logger.info("entry too old {} <= {}", e.aliveUntil, now);
                    entries.remove(e);
                } else {
                    //logger.info("entry ok! {} > {}", e.aliveUntil, now);
                    c++;
                }


            }
            return c;

        }


    }

    private static class MessageTimerEntry {
        public long aliveUntil;

        public MessageTimerEntry() {
            this.aliveUntil = System.currentTimeMillis() + 1000;
        }
    }

}
