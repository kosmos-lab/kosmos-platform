package de.kosmos_lab.kosmos.platform.web;

import javax.websocket.Session;
import javax.annotation.Nonnull;

/**
 * Interface for all WebSocket Services
 */
public abstract  class WebSocketService  {



    /**
     * a new client connected
     *
     * @param sess
     */
    public abstract void addWebSocketClient(Session sess);

    /**
     * the given client left
     *
     * @param sess
     */
    public  abstract  void delWebSocketClient(Session sess);

    /**
     * will be triggered if a new message arrives from a client
     *
     * @param sess
     * @param message
     */
    public abstract void onWebSocketMessage(Session sess, String message);
}
