package de.kosmos_lab.kosmos.platform.smarthome.ha;


import org.json.JSONObject;


public interface HomeAssistantEventConsumer {


    void parse(HomeAssistantClient client, JSONObject object) ;
}
