package de.kosmos_lab.platform.smarthome;

import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.IController;


public abstract class SmartHomeInterface implements CommandInterface {
    private IController controller;
    public abstract boolean getSkipInternal();
    
    public SmartHomeInterface(IController controller) {
        this.controller = controller;
    
        if ( controller != null ) {
            this.controller.addSmartHome(this);
        }
        
    }
    
    
    public void setController(IController controller) {
        this.controller = controller;
        
   }
    
    public IController getController() {
        return this.controller;
    }
    
    /**
     * {"event": "update","device":"context_lamp17","data":{.....},"updatedFrom":"ContextGateway","timeStamp":12489172487124}
     */
    
    
    public Device getDevice(String uuid) throws DeviceNotFoundException {
        return this.controller.getDevice(uuid);
    }
    
    
    public abstract void onConnect();
    
    /**
     * used to signal that a device is now available
     *
     * @param device the device to set online
     */
    public abstract void setOnline(Device device);
    
    
    /*
    
    @EntryPoint /device/<id>/set
    * http device/omni_oma_else/set
    HEADER:
    TOKEN: jwt {"project":"omniconnect",}
    body:
    {"room":"WZ"}
    controller.update(client_id,omni_oma_else,room,wz)
    
     * HTTP /device/config body: {...schema...}
     * WS {"event":"addDevice","value":{...schema...}
     * MQTT /kosmos/deviceid/config payload: {...schema..}
     *
      WS /kosmos/deviceid/config:{"$id":"http://omniconnect.de/schema/#PersonWithRoomInformation","properties":[...]} #AddDeviceConfig <- ERROR_SCHEMA_UNKNOWN
      
      WS /kosmos/deviceid/config:{"$ref":"http://omniconnect.de/schema/#PersonWithRoomInformation"} #AddDeviceConfig
      WS /kosmos/deviceid/set:{"room":"wz"} #updateThing
      
      WS /kosmos/deviceid/status:{...schema..} #statusUpdate
     
     *
     *
     *
     */
  
}
