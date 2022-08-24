package CI.HTTP;

import common.CommonBase;
import common.Utils;
import de.kosmos_lab.web.server.WebServer;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDevice {
    
    @Test(groups = {"deviceTest"}, dependsOnGroups = {"addSchema"})
    public void deviceTest() {
        
        
        ContentResponse response = CommonBase.clientAdmin.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        //Assert.assertEquals(response.getContentAsString(), "[]", "Device list was not empty");
        String uuid = "deviceTestmulti1";
        
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json").put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 50)));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_DUPLICATE, "Device add did NOT fail for a duplicate!");
        response = CommonBase.clientUser.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        String buffer = response.getContentAsString();
        //Assert.assertNotEquals(buffer, "[]", "Device list was not empty");
        JSONArray arr = new JSONArray(buffer);
        //Assert.assertEquals(arr.length(), 1);
        //System.out.println(arr.toString(2));
        boolean found = true;
        JSONObject obj, state;
        for (int i = 0; i < arr.length(); i++) {
            
            obj = arr.getJSONObject(i);
            if (obj.getString("uuid").equals(uuid)) {
                
                Assert.assertEquals(obj.getString("name"), uuid);
                Assert.assertEquals(obj.getString("uuid"), uuid);
                Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/FakeMultiSensor.json");
                state = obj.optJSONObject("state");
                Assert.assertNotNull(state);
                Assert.assertTrue(state.has("currentEnvironmentTemperature"), "currentEnvironmentTemperature not found!");
                Assert.assertEquals(state.getInt("currentEnvironmentTemperature"), 25);
                Assert.assertTrue(state.has("humidityLevel"), "humidityLevel not found!");
                Assert.assertEquals(state.getInt("humidityLevel"), 50);
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list");
        }
        
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        obj = CommonBase.clientUser.fetchJSONObject("/device/get?id=" + uuid,HttpMethod.GET);
        Assert.assertNotNull(obj);

        Assert.assertEquals(obj.getString("name"), uuid);
        Assert.assertEquals(obj.getString("uuid"), uuid);
        Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/FakeMultiSensor.json");
        state = obj.optJSONObject("state");
        Assert.assertNotNull(state);
        Assert.assertEquals(state.getInt("currentEnvironmentTemperature"), 17);
        Assert.assertEquals(state.getInt("humidityLevel"), 50);
        //should fail because this property does not exist!
        String scopeName = "test";
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentenvironmenttemperature", 20));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_VALIDATION_FAILED, "Device set did NOT fail!");
        
        
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", uuid));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device delete NOT failed");
        response = CommonBase.clientUser.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", uuid));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device delete failed");
        
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", uuid).put("scopes", new JSONObject().put("write", scopeName + ":write").put("del", scopeName + ":del").put("read", scopeName + ":read")).put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 100)).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.clientUser2.getResponse("/device/get?id="+uuid, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device was readable!");
        response = CommonBase.clientUser2.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        arr = new JSONArray(response.getContentAsString());
        for (int j = 0; j < arr.length(); j++) {
            obj = arr.getJSONObject(j);
            if (obj.getString("uuid").equalsIgnoreCase(uuid)) {
                Assert.fail("was able to see " + uuid + " in the device list");
                break;
            }
        }
        response = CommonBase.clientUser.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        arr = new JSONArray(response.getContentAsString());
        found = false;
        for (int j = 0; j < arr.length(); j++) {
            obj = arr.getJSONObject(j);
            if (obj.getString("uuid").equalsIgnoreCase(uuid)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list as user2");
        }
        response = CommonBase.clientAdmin.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        arr = new JSONArray(response.getContentAsString());
        found = false;
        for (int j = 0; j < arr.length(); j++) {
            obj = arr.getJSONObject(j);
            if (obj.getString("uuid").equalsIgnoreCase(uuid)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list as admin");
        }
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 14));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set was NOT possible without access!");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 15));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set was possible without access!");
        response = CommonBase.clientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser2.getUserName()).put("scope", scopeName + ":read"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Scope add did fail!");
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + uuid, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was NOT readable!");
        response = CommonBase.clientUser.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        arr = new JSONArray(response.getContentAsString());
        found = false;
        for (int j = 0; j < arr.length(); j++) {
            obj = arr.getJSONObject(j);
            if (obj.getString("uuid").equalsIgnoreCase(uuid)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list");
        }
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 15));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set was possible without access!");
        response = CommonBase.clientAdmin.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser2.getUserName()).put("scope", scopeName + ":write"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Scope add as admin did fail!");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 13));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set was NOT possible with the right access!");
    }
    @Test(groups = {"readOnlyTest"}, dependsOnGroups = {"deviceTest"})
    public void readOnlyTest() {
        
        
        ContentResponse response = CommonBase.clientAdmin.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        //Assert.assertEquals(response.getContentAsString(), "[]", "Device list was not empty");
        String uuid = "occupancy1";
        
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/OccupancySensor.json").put("state", new JSONObject().put("occupancy", true).put("battery", 70)));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/OccupancySensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_DUPLICATE, "Device add did NOT fail for a duplicate!");

        response = CommonBase.clientUser.getResponse("/device/list", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        String buffer = response.getContentAsString();
        //Assert.assertNotEquals(buffer, "[]", "Device list was not empty");
        JSONArray arr = new JSONArray(buffer);
        //Assert.assertEquals(arr.length(), 1);
        //System.out.println(arr.toString(2));
        boolean found = true;
        JSONObject obj, state;
        for (int i = 0; i < arr.length(); i++) {
            
            obj = arr.getJSONObject(i);
            if (obj.getString("uuid").equals(uuid)) {
                
                Assert.assertEquals(obj.getString("name"), uuid);
                Assert.assertEquals(obj.getString("uuid"), uuid);
                Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/OccupancySensor.json");
                state = obj.optJSONObject("state");
                Assert.assertNotNull(state);
                Assert.assertTrue(state.has("occupancy"), "currentEnvironmentTemperature not found!");
                Assert.assertEquals(state.getBoolean("occupancy"), true);
                Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
                Assert.assertEquals(state.getInt("battery"), 70);
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list");
        }
        
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("occupancy", false));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_VALIDATION_FAILED, "Device set did NOT fail!");
        
        obj = CommonBase.clientUser.fetchJSONObject("/device/get?id=" + uuid,HttpMethod.GET);
        Assert.assertNotNull(obj);

        Assert.assertEquals(obj.getString("name"), uuid);
        Assert.assertEquals(obj.getString("uuid"), uuid);
        Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/OccupancySensor.json");
        state = obj.optJSONObject("state");
        Assert.assertNotNull(state);
        Assert.assertTrue(state.has("occupancy"), "currentEnvironmentTemperature not found!");
        Assert.assertEquals(state.getBoolean("occupancy"), true);
        Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
        Assert.assertEquals(state.getInt("battery"), 70);
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", uuid).put("occupancy", false));
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did NOT fail!");
    
        obj = CommonBase.clientUser.fetchJSONObject("/device/get?id=" + uuid,HttpMethod.GET);
        Assert.assertNotNull(obj);

        Assert.assertEquals(obj.getString("name"), uuid);
        Assert.assertEquals(obj.getString("uuid"), uuid);
        Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/OccupancySensor.json");
        state = obj.optJSONObject("state");
        Assert.assertNotNull(state);
        Assert.assertTrue(state.has("occupancy"), "currentEnvironmentTemperature not found!");
        Assert.assertEquals(state.getBoolean("occupancy"), false);
        Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
        Assert.assertEquals(state.getInt("battery"), 70);
        
    }
    @Test(groups = "testChangeSets", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testChangeSets() {
        String deviceName = "FakeMultiSensor124";
        ContentResponse response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.clientUser.getResponse("/obs/live?json&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        JSONObject obj = new JSONObject(response.getContentAsString());
        Assert.assertNotNull(obj, "Response was null?");
        Assert.assertEquals(obj.length(), 0, "Change list was not empty");
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.clientUser.getResponse("/obs/live?maxAge=10&json&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        obj = new JSONObject(response.getContentAsString());
        Assert.assertNotNull(obj, "Response was null?");
        Assert.assertEquals(obj.length(), 1, "Change list did not have 1 entry");
        Assert.assertTrue(obj.has(deviceName), "change set did not have " + deviceName);
        JSONObject c = obj.getJSONObject(deviceName);
        Assert.assertNotNull(c, "change was null");
        Assert.assertTrue(c.has("currentEnvironmentTemperature"), "change did not have currentEnvironmentTemperature " + obj);
        Assert.assertEquals(c.getJSONObject("currentEnvironmentTemperature").getInt("value"), 19);
        response = CommonBase.clientUser.getResponse("/obs/live?maxAge=10&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        String res = response.getContentAsString();
        Assert.assertEquals(res, deviceName + ":{\"currentEnvironmentTemperature\":19}", "did not match " + res);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //change to same value
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        response = CommonBase.clientUser.getResponse("/obs/live?maxAge=10&json&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        obj = new JSONObject(response.getContentAsString());
        Assert.assertNotNull(obj, "Response was null?");
        Assert.assertEquals(obj.length(), 0, "Change list did not have 0 entries " + obj);
        //change to same value
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 20).put("humidityLevel", 70));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.clientUser.getResponse("/obs/live?maxAge=10&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        res = response.getContentAsString();
        //keys might be reversed - should not happen, but still both of those messages are technically valid
        Assert.assertTrue(res.equals(deviceName + ":{\"currentEnvironmentTemperature\":20,\"humidityLevel\":70}") || res.equals(deviceName + ":{\"humidityLevel\":70,\"currentEnvironmentTemperature\":20}"), "did not match " + res);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 20).put("humidityLevel", 80);
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, json);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response = CommonBase.clientUser.getResponse("/obs/live?maxAge=10&uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        res = response.getContentAsString();
        Assert.assertEquals(res, deviceName + ":{\"humidityLevel\":80}", "did not match " + res);
        json.remove("uuid");
        CommonBase.jsonCache.put("state_" + deviceName, json);
        
    }
    
    @Test(groups = "testLocation", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testLocation() {
        String deviceName = "FakeMultiSensor127";
        ContentResponse response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        
        
        response = CommonBase.clientUser.getResponse("/device/location?uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        String r = response.getContentAsString();
        Assert.assertEquals(r, "{}", "did not get an empty json object");
        JSONObject jsonCheck = new JSONObject().put("uuid", deviceName).put("x", 1).put("y", 2).put("z", 3).put("w", 4).put("d", 5).put("h", 6).put("area", "test1");
        response = CommonBase.clientUser.getResponse("/device/location", HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.clientUser.getResponse("/device/location?uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        JSONObject json = new JSONObject(response.getContentAsString());
    
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("uuid", deviceName).put("x", 11).put("y", 12).put("z", 13).put("w", 14).put("d", 15).put("h", 16).put("area", "test2");
        response = CommonBase.clientUser.getResponse("/device/location", HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.clientUser.getResponse("/device/location?uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
    
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("x", 112);
        //create empty json again to check against
        response = CommonBase.clientUser.getResponse("/device/location", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("x", 112));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.clientUser.getResponse("/device/location?uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("roll", 19).put("yaw", 20).put("pitch", 21);
        response = CommonBase.clientUser.getResponse("/device/location", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("roll", 19).put("yaw", 20).put("pitch", 21));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.clientUser.getResponse("/device/location?uuid=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        CommonBase.jsonCache.put("location_" + deviceName, jsonCheck);
        response = CommonBase.clientUser.getResponse("/device/locations", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json.getJSONObject(deviceName), new String[]{"uuid"});
        
        
    }
    
    
}
