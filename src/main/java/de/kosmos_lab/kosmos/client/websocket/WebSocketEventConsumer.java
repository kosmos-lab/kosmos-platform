package de.kosmos_lab.kosmos.client.websocket;

import de.kosmos_lab.kosmos.platform.smarthome.ha.HomeAssistantClient;
import org.json.JSONObject;

public interface WebSocketEventConsumer {

    void parse(WebSocketClientEndpoint client, JSONObject object);
}
