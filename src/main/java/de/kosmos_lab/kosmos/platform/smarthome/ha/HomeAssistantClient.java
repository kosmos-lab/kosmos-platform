package de.kosmos_lab.kosmos.platform.smarthome.ha;

import de.kosmos_lab.kosmos.client.websocket.WebSocketEventConsumer;
import de.kosmos_lab.utils.JSONChecker;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class HomeAssistantClient extends Endpoint {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("HomeAssistantClient");
    private final HomeAssistantHTTPClient haclient;
    public CountDownLatch initLatch;
    protected boolean stopped = false;
    int lastId = 1;
    private Session session;
    private boolean authed = false;
    private final JSONObject vars = new JSONObject();
    private final HashMap<Integer, HomeAssistantEventConsumer> consumers = new HashMap<>();
    
    public HomeAssistantClient(HomeAssistantHTTPClient haclient) {
        this.haclient = haclient;
        this.initLatch = new CountDownLatch(1);

    }
    
    public void addConsumer(int id, HomeAssistantEventConsumer consumer) {
        this.consumers.put(id, consumer);
        
        
    }
    
    public Object getVar(String name) {
        return this.vars.get(name);
    }
    public JSONObject getVars() {
        return this.vars;
    }
    
    public boolean isAuthed() {
        return this.authed;
    }
    
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        logger.info("HA onClose");
        //this.stopped = false;
    }
    
    public void onMessageReceived(String message) {
        
        
        onMessageReceived(new JSONObject(message));
        
    }

    public void onMessageReceived(JSONObject json) {
        logger.info("wsreceived: {}",json.toString());
        
        try {
            
            if (json.has("id")) {
                Integer id = json.getInt("id");
                HomeAssistantEventConsumer consumer = consumers.get(id);
                if (consumer != null) {
                    logger.info("found consumer");
                    consumer.parse(this, json);
                }
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

        try {
            logger.info("HAClient onOpen");
            this.session = session;
            
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                
                @Override
                public void onMessage(String message) {
                    logger.trace("HAClient onMessage {}", message);
                    JSONObject json = new JSONObject(message);
                    if (json.has("type")) {
                        String type = json.getString("type");
                        
                        if (type.equals("auth_required")) {
                            authed = false;
                            //we need to authenticate ourself
                            try {
                                send("{\"type\": \"auth\", \"access_token\": \"" + haclient.getToken() + "\"}");
                                return;
                            } catch (IOException e) {
                                logger.error("Exception!", e);
                            }
                            
                        } else  if (type.equals("auth_ok")) {

                            authed = true;
    
                        }
                        
                        
                    }
                    //if the returned json has an id, we know its a response - handle it accordingly
                    if (json.has("id")) {
                        onMessageReceived(json);
                    }
                    
                }
            });
        } catch (Exception e) {
            logger.error("Exception!", e);
        }
    }
    
    
    /**
     * sends the given text to the Endpoint
     *
     * @param text
     * @throws IOException
     */
    public void send(String text) throws IOException {
        
        session.getBasicRemote().sendText(text);
    }
    
    public void sendCommand(JSONObject command, HomeAssistantEventConsumer consumer) {
        if (command.has("id")) {
            try {
                int id = command.getInt("id");
                if (id > lastId) {
                    lastId = id;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!command.has("id")) {
            command.put("id", ++lastId);
        }
        this.addConsumer(command.getInt("id"), consumer);
        try {
            logger.info("wssent: {}", command);
    
            this.send(command.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public boolean waitForValue( String key, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        while (true) {
            try {
                
                if (JSONChecker.equals(vars.get(key), expected)) {
                    logger.info("FOUND MATCH FOR {} - it seems to be",key,expected);
                    return true;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
    
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long delta = System.currentTimeMillis() - started;
            if (delta > waittime) {
                logger.info("GAVE UP FOR {}",key);

                return false;
            }
        }
    }
    
    
    public void setVar(String name, Object value) {
        logger.info("setting {} to {}",name,value);
        this.vars.put(name, value);
    }
    
    public void stop() {
        this.stopped = true;
        try {
            this.session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
