package de.kosmos_lab.platform.persistence;

import java.util.regex.Pattern;

public class Constants {

    public enum RunMode {NORMAL, TEST,PLUGIN_TEST}
    public enum PersistenceMode { MEMORY_ONLY,MEMORY_AND_PERSISTENCE}
    public enum CacheMode { CACHE_ONLY,CACHE_AND_PERSISTENCE}
    public static final String dateFilePattern = "yyyy-MM-dd_HH-mm-ss";
    public static final String recordingDir = "recordings/{user}";
    public static final String recordingFile = recordingDir+"/{cam}_{date}.mp4";

    public final static String formatDate = "yyyy-MM-dd";
    public final static String formatJSONDate = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String formatTime = "HH:mm:ss";
    public final static String formatDateAndTime = "yyyy-MM-dd HH:mm:ss";
    public final static Pattern websocketSplitPattern = Pattern.compile("^(.*?):(.*)$");
    public final static Pattern domainSplitPattern = Pattern.compile("^(?<domain>alarm_control_panel|alert|alexa|automation|binary_sensor|camera|climate|device_tracker|ENTITY|fan|group|image_processing|input_boolean|input_select|light|media_player|person|remote|script|sensor|sun|switch|timer|weather|zigbee2mqtt_networkmap|zone)\\.(?<uuid>.*)$");
    
    public final static String pathTestGesture = "testgestures";
    public final static String pathProdGesture = "config/gestures";
}
