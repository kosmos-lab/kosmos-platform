package de.kosmos_lab.kosmos.platform.web;

import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value = "/defaultws") //will be overwritten
public class KosmoSWebSocketEndpoint {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSWebSocketEndpoint");
    private final WebSocketService service;
    private Session sess;
    
    
    public KosmoSWebSocketEndpoint(WebSocketService service) {
        
        
        this.service = service;
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        
        logger.info("Socket Closed: {} ", reason);
        this.service.delWebSocketClient(sess);
        
    }
    
    @OnOpen
    public void onWebSocketConnect(Session sess) {
        this.sess = sess;
        this.service.addWebSocketClient(sess);
        
    }
    
    @OnError
    public void onWebSocketError(Throwable cause) {
        this.service.delWebSocketClient(sess);
        if (cause instanceof java.util.concurrent.TimeoutException) {
            return;
        }
        if (cause.getCause() instanceof java.util.concurrent.TimeoutException) {
            return;
        }
        if (cause instanceof org.eclipse.jetty.io.EofException) {
            return;
        }
        logger.error("Exception", cause);
        
    }
    
    @OnMessage
    public void onWebSocketText(String message) {
        
        this.service.onWebSocketMessage(sess, message);
    }
    
    
}
