package de.kosmos_lab.kosmos.platform.web;

import javax.websocket.server.ServerEndpointConfig;

public class KosmoSWebSocketEndpointConfiguration extends ServerEndpointConfig.Configurator {
    
    
    private final WebSocketService service;
    
    public KosmoSWebSocketEndpointConfiguration(WebSocketService service) {
        this.service = service;
    }
    
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return (T) new KosmoSWebSocketEndpoint(service);
    }
}
