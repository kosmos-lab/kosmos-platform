package de.kosmos_lab.platform.data;

import org.json.JSONObject;

public class DeviceText {
    public Device getDevice() {
        return device;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    private final Device device;
    private final String key;
    private String value;

    public DeviceText(Device device, String key, String value) {
        this.device = device;
        this.key = key;
        this.value = value;
        this.device.addText(this);
    }

    public JSONObject toJSON() {
        return new JSONObject().put("key", key).put("value", value);
    }


    public void setValue(String value) {
        this.value = value;
    }
}
