package common;

import de.kosmos_lab.kosmos.platform.utils.KosmoSHelper;
import de.kosmos_lab.utils.JSONChecker;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    /**
     * check if we are NOT in the docker CI
     * @return
     */
    public static boolean skip_docker_creation() {
        return KosmoSHelper.getEnvBool("SKIP_DOCKER_CREATION");

    }
    public static boolean compare(JSONObject a, JSONObject b, Set<String> skippedKeys) {
        for (String k : a.keySet()) {
            if (!skippedKeys.contains(k)) {
                try {
                    if (!JSONChecker.checkValue(b, k, a.get(k))) {
                        Assert.fail("key: " + k + " did not match!" + b.get(k) + " - " + a.get(k));
                        return false;
                    }
                } catch (Exception e) {
                    Assert.fail("exception while checking key: " + k);
                    e.printStackTrace();
                    return false;
                }
                
            }
        }
        return true;
    }
    
    public static boolean compare(JSONObject a, JSONObject b, String[] skippedKeys) {
        Set<String> keys = new HashSet<>();
        
        for (String k : skippedKeys) {
            keys.add(k);
        }
        
        return compare(a, b, keys);
    }
    
    public static boolean compare(JSONObject a, JSONObject b) {
        return compare(a, b, new HashSet<String>());
    }
    
}
