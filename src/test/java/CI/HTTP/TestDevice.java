package CI.HTTP;

import common.CommonBase;
import common.Utils;
import de.kosmos_lab.platform.client.KosmoSPathHelper;
import de.kosmos_lab.platform.web.servlets.device.DeviceAddServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceDeleteServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceGetServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceListServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceLocationServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceLocationsServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceSetServlet;
import de.kosmos_lab.platform.web.servlets.device.DeviceSetTextServlet;
import de.kosmos_lab.platform.web.servlets.obs.OBSLiveServlet;
import de.kosmos_lab.platform.web.servlets.scope.ScopeAddUserServlet;
import de.kosmos_lab.web.server.WebServer;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDevice {

    public static final String texts_device_name = "FakeMultiSensor128";


    public static void retest() {
        ContentResponse response = CommonBase.httpClientAdmin.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new JSONObject().put("uuid", texts_device_name));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device get failed!");
        JSONObject dev = new JSONObject(response.getContentAsString());
        Assert.assertTrue(dev.has("texts"));
        JSONObject texts = dev.optJSONObject("texts");
        Assert.assertNotNull(texts);
        Assert.assertEquals(texts.getString("description"), "test2");

    }

    @Test(groups = {"deviceTest"}, dependsOnGroups = {"addSchema"})
    public void deviceTest() {


        ContentResponse response = CommonBase.httpClientAdmin.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        //Assert.assertEquals(response.getContentAsString(), "[]", "Device list was not empty");
        String uuid = "deviceTestmulti1";

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json").put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 50)));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_DUPLICATE, "Device add did NOT fail for a duplicate!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        obj = CommonBase.httpClientUser.fetchJSONObject(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new String[][]{{"id", uuid}});
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentenvironmenttemperature", 20));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_VALIDATION_FAILED, "Device set did NOT fail!");


        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceDeleteServlet.class), HttpMethod.DELETE, new JSONObject().put("id", uuid));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device delete NOT failed");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceDeleteServlet.class), HttpMethod.DELETE, new JSONObject().put("id", uuid));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device delete failed");

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", uuid).put("scopes", new JSONObject().put("write", scopeName + ":write").put("del", scopeName + ":del").put("read", scopeName + ":read")).put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 100)).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new String[][]{{"id", uuid}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device was readable!");
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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
        response = CommonBase.httpClientAdmin.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 14));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set was NOT possible without access!");
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 15));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set was possible without access!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(ScopeAddUserServlet.class), HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser2.getUserName()).put("scope", scopeName + ":read"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Scope add did fail!");
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new String[][]{{"id", uuid}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was NOT readable!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 15));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set was possible without access!");
        response = CommonBase.httpClientAdmin.getResponse(KosmoSPathHelper.getPath(ScopeAddUserServlet.class), HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser2.getUserName()).put("scope", scopeName + ":write"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Scope add as admin did fail!");
        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("currentEnvironmentTemperature", 13));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set was NOT possible with the right access!");
    }

    @Test(groups = {"readOnlyTest"}, dependsOnGroups = {"deviceTest"})
    public void readOnlyTest() {


        ContentResponse response = CommonBase.httpClientAdmin.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device list was not possible!");
        //Assert.assertEquals(response.getContentAsString(), "[]", "Device list was not empty");
        String uuid = "occupancy1";

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/OccupancySensor.json").put("state", new JSONObject().put("occupancy", true).put("battery", 70)));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", uuid).put("schema", "https://kosmos-lab.de/schema/OccupancySensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_DUPLICATE, "Device add did NOT fail for a duplicate!");

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceListServlet.class), HttpMethod.GET);
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
                Assert.assertTrue(state.getBoolean("occupancy"));
                Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
                Assert.assertEquals(state.getInt("battery"), 70);
            }
        }
        if (!found) {
            Assert.fail("could not see " + uuid + " in Device list");
        }

        response = CommonBase.httpClientUser2.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("occupancy", false));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_VALIDATION_FAILED, "Device set did NOT fail!");

        obj = CommonBase.httpClientUser.fetchJSONObject(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new String[][]{{"id", uuid}});
        Assert.assertNotNull(obj);

        Assert.assertEquals(obj.getString("name"), uuid);
        Assert.assertEquals(obj.getString("uuid"), uuid);
        Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/OccupancySensor.json");
        state = obj.optJSONObject("state");
        Assert.assertNotNull(state);
        Assert.assertTrue(state.has("occupancy"), "currentEnvironmentTemperature not found!");
        Assert.assertTrue(state.getBoolean("occupancy"));
        Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
        Assert.assertEquals(state.getInt("battery"), 70);
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("id", uuid).put("occupancy", false));
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did NOT fail!");

        obj = CommonBase.httpClientUser.fetchJSONObject(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new String[][]{{"id", uuid}});
        Assert.assertNotNull(obj);

        Assert.assertEquals(obj.getString("name"), uuid);
        Assert.assertEquals(obj.getString("uuid"), uuid);
        Assert.assertEquals(obj.getString("schema"), "https://kosmos-lab.de/schema/OccupancySensor.json");
        state = obj.optJSONObject("state");
        Assert.assertNotNull(state);
        Assert.assertTrue(state.has("occupancy"), "currentEnvironmentTemperature not found!");
        Assert.assertFalse(state.getBoolean("occupancy"));
        Assert.assertTrue(state.has("battery"), "humidityLevel not found!");
        Assert.assertEquals(state.getInt("battery"), 70);

    }

    @Test(groups = "testChangeSets", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testChangeSets() {
        String deviceName = "FakeMultiSensor124";
        ContentResponse response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"json", "true"}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        JSONObject obj = new JSONObject(response.getContentAsString());
        Assert.assertNotNull(obj, "Response was null?");
        Assert.assertEquals(obj.length(), 0, "Change list was not empty");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"json", "true"}, {"maxAge", "10"}});
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"maxAge", "10"}});
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"json", "true"}, {"maxAge", "10"}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "obs live did fail!");
        obj = new JSONObject(response.getContentAsString());
        Assert.assertNotNull(obj, "Response was null?");
        Assert.assertEquals(obj.length(), 0, "Change list did not have 0 entries " + obj);
        //change to same value
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("currentEnvironmentTemperature", 20).put("humidityLevel", 70));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"maxAge", "10"}});
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
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetServlet.class), HttpMethod.POST, json);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(OBSLiveServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}, {"maxAge", "10"}});
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
        ContentResponse response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");


        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        String r = response.getContentAsString();
        Assert.assertEquals(r, "{}", "did not get an empty json object");
        JSONObject jsonCheck = new JSONObject().put("uuid", deviceName).put("x", 1).put("y", 2).put("z", 3).put("w", 4).put("d", 5).put("h", 6).put("area", "test1");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        JSONObject json = new JSONObject(response.getContentAsString());

        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("uuid", deviceName).put("x", 11).put("y", 12).put("z", 13).put("w", 14).put("d", 15).put("h", 16).put("area", "test2");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());

        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("x", 112);
        //create empty json again to check against
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("x", 112));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        jsonCheck.put("roll", 19).put("yaw", 20).put("pitch", 21);
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("roll", 19).put("yaw", 20).put("pitch", 21));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device set location failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationServlet.class), HttpMethod.GET, new String[][]{{"uuid", deviceName}});
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json, new String[]{"uuid"});
        CommonBase.jsonCache.put("location_" + deviceName, jsonCheck);
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceLocationsServlet.class), HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "location did fail!");
        json = new JSONObject(response.getContentAsString());
        Utils.compare(jsonCheck, json.getJSONObject(deviceName), new String[]{"uuid"});


    }

    @Test(groups = "testLabel", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testLabel() {

        ContentResponse response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceAddServlet.class), HttpMethod.POST, new JSONObject().put("uuid", texts_device_name).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");

        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new JSONObject().put("uuid", texts_device_name));
        Assert.assertNotNull(response);
        JSONObject dev = new JSONObject(response.getContentAsString());
        Assert.assertEquals(dev.getString("uuid"), texts_device_name);
        Assert.assertFalse(dev.has("texts"));

        JSONObject jsonCheck = new JSONObject().put("uuid", texts_device_name).put("key", "description").put("value", "test");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetTextServlet.class), HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "device set text failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new JSONObject().put("uuid", texts_device_name));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device get failed!");
        dev = new JSONObject(response.getContentAsString());
        Assert.assertTrue(dev.has("texts"));
        JSONObject texts = dev.optJSONObject("texts");
        Assert.assertNotNull(texts);
        Assert.assertEquals(texts.getString("description"), "test");


        jsonCheck = new JSONObject().put("uuid", texts_device_name).put("key", "description").put("value", "test2");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceSetTextServlet.class), HttpMethod.POST, jsonCheck);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "device set text failed!");
        response = CommonBase.httpClientUser.getResponse(KosmoSPathHelper.getPath(DeviceGetServlet.class), HttpMethod.GET, new JSONObject().put("uuid", texts_device_name));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "device get failed!");
        dev = new JSONObject(response.getContentAsString());
        Assert.assertTrue(dev.has("texts"));
        texts = dev.optJSONObject("texts");
        Assert.assertNotNull(texts);
        Assert.assertEquals(texts.getString("description"), "test2");


    }

}
