package de.kosmos_lab.platform.utils;

import java.util.concurrent.ConcurrentHashMap;

public class KosmoSHelper {
    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    public static String getEnv(String key) {
        return map.getOrDefault(key, System.getenv(key));

    }
    public static boolean getEnvBool(String key) {
        String value = map.getOrDefault(key, System.getenv(key));
        if ( value != null ) {
            if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
                return true;
            }

        }
        return false;

    }

    public static void setEnv(String key, String value) {
        map.put(key, value);

    }
}
