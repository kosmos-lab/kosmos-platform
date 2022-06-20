package de.kosmos_lab.kosmos.platform.smarthome;

import de.kosmos_lab.kosmos.data.Device;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collections;
import java.util.Set;


public class PropertyTranslator {
    protected static final Logger logger = LoggerFactory.getLogger("PropertyTranslator");
    
    /**
     * transforms the attributes if needed
     * used to transform between special states like dimmingLevel and brightness
     * @param device
     * @param json
     * @param keys
     * @return
     */
    public static boolean transform(Device device, JSONObject json, Set<String> keys) {
        
        for (String key : keys) {
            logger.trace("transforming key {}",key);
            if (json.has("rgb_color")) {
                
                Object arr = json.get("rgb_color");
                if (arr instanceof JSONArray) {
                    json.put("r", ((JSONArray) arr).getInt(0));
                    json.put("g", ((JSONArray) arr).getInt(1));
                    json.put("b", ((JSONArray) arr).getInt(2));
                    json.remove("rgb_color");
                    transform(device, json, Collections.singleton("r"));
                }
                continue;
            }
            if (key.equalsIgnoreCase("brightness")) {
                
                if (device.canHave("dimmingLevel", false)) {
                    json.put("dimmingLevel", (int) (json.optDouble("brightness",255) / 2.55));
                    json.remove("brightness");
                }
                else if ( device.has("color")) {
                    JSONObject color = device.optJSONObject("color");
                    if ( color.has("r") && color.has("g")&& color.has("b")) {
                        color.put("r", color.getInt("r")*device.getInt("brightness")/255);
                        color.put("g", color.getInt("g")*device.getInt("brightness")/255);
                        color.put("b", color.getInt("b")*device.getInt("brightness")/255);
                    }
                }
                continue;
            }
            
            if (key.equalsIgnoreCase("red")) {
                
                if (json.has("green") && json.has("blue")) {
                    if (device.canHave("hue", false) && device.canHave("saturation", false)) {
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(json.getInt("red"), json.getInt("green"), json.getInt("blue"), hsv);
                        json.remove("red");
                        json.remove("green");
                        json.remove("blue");
                        json.put("hue", hsv[0] * 360);
                        json.put("saturation", hsv[1] * 100);
                        json.put("brightness", hsv[2] * 255);
                        transform(device, json, Collections.singleton("brightness"));
                    } else {
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(json.getInt("red"), json.getInt("green"), json.getInt("blue"), hsv);
                        json.remove("red");
                        json.remove("green");
                        json.remove("blue");
                        json.put("hs_color", new JSONArray().put(hsv[0] * 360).put(hsv[1] * 100));
                        json.put("brightness", hsv[2] * 255);
                        transform(device, json, Collections.singleton("brightness"));
                    }
                }
                continue;
            }
            if (key.equalsIgnoreCase("r")) {
                
                if (json.has("g") && json.has("b")) {
                    if (device.canHave("color", false)) {
                        json.put("color", new JSONObject().put("r", json.getInt("r")).put("g", json.getInt("g")).put("b", json.getInt("b")));
                        json.remove("r");
                        json.remove("g");
                        json.remove("b");
                        transform(device, json, Collections.singleton("brightness"));
                    } else if (device.canHave("hue", false) && device.canHave("saturation", false)) {
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(json.getInt("r"), json.getInt("g"), json.getInt("b"), hsv);
                        json.remove("r");
                        json.remove("g");
                        json.remove("b");
                        json.put("hue", hsv[0] * 360);
                        json.put("saturation", hsv[1] * 100);
                        json.put("brightness", hsv[2] * 255);
                        transform(device, json, Collections.singleton("brightness"));
                        
                    } else {
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(json.getInt("r"), json.getInt("g"), json.getInt("b"), hsv);
                        json.remove("r");
                        json.remove("g");
                        json.remove("b");
                        json.put("hs_color", new JSONArray().put(hsv[0] * 360).put(hsv[1] * 100));
                        
                        transform(device, json, Collections.singleton("brightness"));
                    }
                }
                continue;
            }
        }
        
        
        return false;
    }
    
}
