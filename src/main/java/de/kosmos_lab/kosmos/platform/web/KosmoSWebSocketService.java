package de.kosmos_lab.kosmos.platform.web;

import de.dfki.baall.helper.webserver.JWT;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.UserNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants;

import de.kosmos_lab.kosmos.platform.rules.RulesService;
import de.kosmos_lab.kosmos.platform.smarthome.CommandInterface;
import de.kosmos_lab.kosmos.platform.smarthome.CommandSourceName;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;


/**
 * This is the Service used to control the KosmoS Websocket on /ws
 */
@ServerEndpoint("/ws")
@WebSocket
public class KosmoSWebSocketService extends WebSocketService implements CommandInterface {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSWebSocketService");
    private final IController controller;
    private final WebServer server;
    public HashMap<String, MessageTimer> messageTimers = new HashMap<>();

    private final HashMap<Session, IUser> webSocketClients = new HashMap<Session, IUser>();
    private final HashMap<Session, IUser> logins = new HashMap<Session, IUser>();
    private final HashMap<Session, String> types = new HashMap<Session, String>();

    public KosmoSWebSocketService(WebServer server,IController c) {
        this.controller = c;
        this.server = server;
        controller.addCommandInterface(this);
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
        synchronized (webSocketClients) {

            for (Map.Entry<Session, IUser> e : this.webSocketClients.entrySet()) {
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
        synchronized (webSocketClients) {

            for (Map.Entry<Session, IUser> e : this.webSocketClients.entrySet()) {
                try {
                    IUser user = e.getValue();
                /*if ( source != null && source == getSourceName(e.getKey())) {
                    continue;
                }*/
                    if (user != null && device.canRead(user)) {
                        try {
                            if (source.getSourceName().startsWith("haset")) {
                                String type = this.types.get(e.getKey());

                                if ("HAIntegration".equals(type) || user.getName().equalsIgnoreCase("ha")) {
                                    logger.trace("SKIPPING broadcast, because it came from HASET in the first place");
                                    continue;
                                }
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
    public void stop() {
        synchronized (webSocketClients) {

            for (Map.Entry<Session, IUser> e : this.webSocketClients.entrySet()) {
                try {
                    e.getKey().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }




    @OnWebSocketConnect
    public void addWebSocketClient(Session sess) {

        synchronized (webSocketClients) {
            this.webSocketClients.put(sess, null);
        }
        //logger.trace("new Websocket connection from {}", sess.getUserProperties().get("jakarta.websocket.endpoint.remoteAddress"));

    }


    @OnWebSocketClose
    public void delWebSocketClient(Session sess) {
        synchronized (webSocketClients) {
            this.webSocketClients.remove(sess);
        }
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
            if (topic.equals("user/auth")) {
                JSONObject json = new JSONObject(payload);
                String usern = json.optString("user");
                String pass = json.optString("pass");
                if (usern != null && pass != null) {
                    IUser u = controller.tryLogin(usern, pass);
                    if (u != null) {
                        webSocketClients.put(sess, u);
                        logins.put(sess, u);
                        logger.info("auth successful");
                        afterAuth(sess, u);
                        return;
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
                        webSocketClients.put(sess, u);
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
                                s.broadcastToUser(webSocketClients.get(sess), obj.toString());
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
                                s.broadcastToUser(webSocketClients.get(sess), obj.toString());
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

                    }

                } else {
                    logger.warn("user is NOT authed!");
                }
            }
        }
        //broadCast(message, sess);
    }
    HashMap<IUser,HashSet<Device>> ignoredDevices = new HashMap<>();
    public void addIgnoredDevice(IUser user,Device device) {
        HashSet<Device> set = this.ignoredDevices.get(user);
        if (set == null) {
            set = new HashSet<>();
            this.ignoredDevices.put(user,set);
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

            return;
        } catch (Exception e) {
            logger.warn("Exception:", e);
        }
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

    @Override
    public String getSourceName() {
        return "HTTPApi";
    }
}
