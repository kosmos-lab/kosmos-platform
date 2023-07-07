package Integration;

import common.CommonBase;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HomeAssistantTests {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("HomeAssistantTests");

    public static Device addDevice(String uuid, File f, JSONObject state) {
        JSONObject schema = addSchema(f);
        return addDevice(uuid, schema.getString("$id"), state);
    }

    public static Device addDevice(String uuid, String schema, JSONObject state) {
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {


            JSONObject json = new JSONObject().put("uuid", uuid).put("schema", schema);
            if (state != null) {
                json.put("state", state);
            }

            ContentResponse response = CommonBase.httpClientAdmin.getResponse("/device/add", HttpMethod.POST, json);
            Assert.assertNotNull(response);

            //Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
            if (response.getStatus() != 204 && response.getStatus() != 409) {

                Assert.fail("Could not add Device " + uuid + " " + response.getStatus() + ":" + response.getContentAsString() + "\n" + json.toString());

            }
            try {
                d = CommonBase.controller.getDevice(uuid);
            } catch (DeviceNotFoundException exx) {
                Assert.fail("Device could not be found again!");
            }

        }
        return d;
    }

    public static JSONObject addSchema(File f) {

        JSONObject schema = null;
        try {
            schema = new JSONObject(new JSONTokener(new FileReader(f, StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            Assert.fail("Could not load Schema from file " + f.getName() + ":" + e.getCause());
        } catch (IOException e) {
            Assert.fail("Could not load Schema from file " + f.getName() + ":" + e.getCause());
        }
        logger.info("getting schema/get?" + UrlEncoded.encodeString(schema.getString("$id")));
        ContentResponse response = CommonBase.httpClientAdmin.getResponse("/schema/get?id=" + UrlEncoded.encodeString(schema.getString("$id")), HttpMethod.GET);
        Assert.assertNotNull(response);

        if (response.getStatus() != 200) {


            response = CommonBase.httpClientAdmin.getResponse("/schema/add", HttpMethod.POST, schema);
            Assert.assertNotNull(response);

            if (response.getStatus() != 200 && response.getStatus() != 409) {
                Assert.fail("Could not add schema: " + f.getName() + "!" + response.getStatus());
            }

            response = CommonBase.httpClientAdmin.getResponse("/schema/get?id=" + UrlEncoded.encodeString(schema.getString("$id")), HttpMethod.GET);
            Assert.assertNotNull(response);

            if (response.getStatus() != 200) {
                Assert.fail("Could not find schema after adding!: " + schema.getString("$id") + "!");
            }
        }
        return schema;

    }

    //@Test(dependsOnGroups = "MQTT")
    public void testLight() {

        String deviceName = "hsvTest";
        String HADeviceName = "light.hsvtest_on";
        Device device = addDevice(deviceName, new File("schema/HSVLamp.json"), new JSONObject().put("hue", 20.0).put("dimmingLevel", 50).put("saturation", 25).put("on", false));
        Assert.assertTrue(CommonBase.waitForValue(device, "saturation", 25, 5000), "Could not verify that value  (saturation) was created in kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "hue", 20, 5000), "Could not verify that value (hue) changed was created in  kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "dimmingLevel", 50, 5000), "Could not verify that value (dimmingLevel) changed was created in kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "on", false, 5000), "Could not verify that value (on) changed was created in kosmos");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ContentResponse response = CommonBase.clientha.getResponse("/api/services/light/turn_on", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName).put("hs_color", new JSONArray().put(145).put(100)));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Could not set light via HA Rest Api (" + response.getStatus() + "): " + response.getContentAsString());
        Assert.assertTrue(CommonBase.waitForValue(device, "saturation", 100, 5000), "Could not verify that value  (saturation) changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "hue", 145, 5000), "Could not verify that value (hue) changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "on", true, 5000), "Could not verify that value (on) changed was propagated back to kosmos");
        response = CommonBase.clientha.getResponse("/api/services/light/turn_on", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName).put("brightness", 255));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Could not set light via HA Rest Api (" + response.getStatus() + "): " + response.getContentAsString());
        //internaly saved as "percentage", so 255 means 100%
        Assert.assertTrue(CommonBase.waitForValue(device, "dimmingLevel", 100, 5000), "Could not verify that value (dimmingLevel) changed was propagated back to kosmos");

        response = CommonBase.clientha.getResponse("/api/services/light/turn_off", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Could not set light via HA Rest Api (" + response.getStatus() + "): " + response.getContentAsString());

        Assert.assertTrue(CommonBase.waitForValue(device, "on", false, 5000), "Could not verify that value (on) changed was propagated back to kosmos");
        try {
            //wait a short time so everything is propagated
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("HATest: switching ON");
        response = CommonBase.clientha.getResponse("/api/services/light/turn_on", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Could not set light via HA Rest Api (" + response.getStatus() + "): " + response.getContentAsString());
        Assert.assertTrue(CommonBase.waitForValue(device, "on", true, 5000), "Could not verify that value (on) changed was propagated back to kosmos");
        response = CommonBase.clientha.getResponse("/api/services/light/turn_on", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName).put("hs_color", new JSONArray().put(360).put(50)).put("brightness", 128));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Could not set light via HA Rest Api (" + response.getStatus() + "): " + response.getContentAsString());
        Assert.assertTrue(CommonBase.waitForValue(device, "saturation", 50, 5000), "Could not verify that value  (saturation) changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "hue", 360, 5000), "Could not verify that value (hue) changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "on", true, 5000), "Could not verify that value (on) changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValue(device, "dimmingLevel", 50, 5000), "Could not verify that value (dimmingLevel) changed was propagated back to kosmos");

    }

    //@Test(dependsOnGroups = "MQTT")
    public void testHeating() {
        String deviceName = "heatertest";
        String HADeviceName = "climate.heatertest_heatingTemperatureSetting";

        Device device = addDevice(deviceName, new File("schema/Heater.json"), new JSONObject().put("heatingTemperatureSetting", 20.0).put("valvePosition", 50));
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "temperature", 20.0, 5000), "Did not find value for temp 20 again");
        ContentResponse response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("heatingTemperatureSetting", 22));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "temperature", 22.0, 5000), "Did not find value for temp 22 again");
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("heatingTemperatureSetting", 24.791));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "temperature", 25.0, 5000), "Did not find value for temp 25 again");
        response = CommonBase.clientha.getResponse("/api/services/climate/set_temperature", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName).put("temperature", 24.0));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "could not set Temperature via HA Restapi");
        Assert.assertTrue(CommonBase.waitForValue(device, "heatingTemperatureSetting", 24.0, 5000), "Could not verify that value changed was changed back to kosmos");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "temperature", 24.0, 5000), "Did not find value for temp 24 again");

        response = CommonBase.clientha.getResponse("/api/services/climate/set_temperature", HttpMethod.POST, new JSONObject().put("entity_id", HADeviceName).put("temperature", 26.124));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "could not set Temperature via HA Restapi");
        Assert.assertTrue(CommonBase.waitForValue(device, "heatingTemperatureSetting", 26.0, 5000), "Could not verify that value changed was propagated back to kosmos");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "temperature", 26.0, 5000), "Did not find value for temp 24 again");


        //the valveposition is its own device for now
        HADeviceName = "sensor.heatertest_valvePosition";
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 50.0, 5000), "Did not find value for valvePosition 50 again");
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("valvePosition", 40.0));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 40.0, 5000), "Did not find value for valvePosition 40 again");

        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("valvePosition", 22.0));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 20.0, 5000), "Did not find value for valvePosition 20 again");


    }

    //@Test(dependsOnGroups = "MQTT")
    public void testMultiSensor() {

        String deviceName = "multihatest";
        
        /*Device device = CommonBase.controller.getDevice(deviceName);
        
        if (device == null) {
            
            JSONObject schema = addSchema(new File("schema/MultiSensor.json"));
            
            //ContentResponse response = CommonBase.clientAdmin.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("schema", schema.getString("$id")));
            //Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
            device = addDevice(deviceName, schema.getString("$id"), null);
            
        }*/

        Device device = addDevice(deviceName, new File("schema/MultiSensor.json"), new JSONObject().put("currentEnvironmentTemperature", 20.0));
    
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        Assert.assertNotNull(device, "Device was empty!");
        //device.set("currentEnvironmentTemperature", 20, true);
        String HADeviceName = "sensor.multihatest_currentenvironmenttemperature";
        long started = System.currentTimeMillis();
        while (true) {
            long delta = System.currentTimeMillis() - started;
            JSONObject obj = CommonBase.clientha.fetchJSONObject("/api/states/" + HADeviceName, HttpMethod.GET, null);

            if (obj == null) {
                if (delta > 15000) {

                    Assert.fail("could not find device in HA");

                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 20.0, 5000), "Did not find value for temp 18 again");
        ContentResponse response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 18.0, 5000), "Did not find value for temp 18 again");
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 22));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 22.0, 5000), "Did not find value for temp 22 again");
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 24.791));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        Assert.assertTrue(CommonBase.waitForValueJSONHttp(CommonBase.clientha, "/api/states/" + HADeviceName, "state", 24.791, 5000), "Did not find value for temp 22 again");
        //logger.info("HADevice: {}",hadevice.state);
    
        /*Assert.assertTrue(CommonBase.waitForValue(hadevice,"currentEnvironmentTemperature",18.0,10000),"HA did not receive update?! "+hadevice.get("currentEnvironmentTemperature"));
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 22.0));
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        
        Assert.assertTrue(CommonBase.waitForValue(hadevice,"currentEnvironmentTemperature",22d,10000),"HA did not receive update?! 22 "+hadevice.get("currentEnvironmentTemperature"));
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 24.791));
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        
        Assert.assertTrue(CommonBase.waitForValue(hadevice,"currentEnvironmentTemperature",25.0,10000),"HA did not receive update?! 25 "+hadevice.get("currentEnvironmentTemperature"));
        System.out.println("test DONE 3");*/

    }


}
