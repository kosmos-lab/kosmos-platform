package Integration;

import common.CommonBase;
import common.Utils;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.smarthome.ha.HomeAssistantClient;
import de.kosmos_lab.platform.smarthome.ha.HomeAssistantHTTPClient;
import de.kosmos_lab.utils.JSONChecker;
import de.kosmos_lab.utils.exceptions.CompareException;
import me.bazhenov.docker.Container;
import me.bazhenov.docker.KosmosDockerTestNgListener;
import me.bazhenov.docker.Port;
import me.bazhenov.docker.Volume;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Container(name = "ha_integration_test", image = "homeassistant/home-assistant:2022.6", publish = @Port(value = 8123, atHost = 48123), volumes = {@Volume(value = "/config", atHost = "docker/ha/testconfig"), @Volume(value = "/config/custom_components", atHost = "docker/ha/config/custom_components")}, removeAfterCompletion = false)

@Listeners(KosmosDockerTestNgListener.class)
public class HAClientTest {
    private static final boolean SkipRetest = false;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("HAClientTest");
    private static final int WAITTIME = 10;
    static ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);
    private static HomeAssistantHTTPClient haclient = null;

    public static void callServiceOnHA(String domain, String service, JSONObject data) {

        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", domain).put("service", service).put("service_data", data), (client, json) -> {
            if (json.has("success")) {

                Assert.assertTrue(json.getBoolean("success"), "could not call service");
                //haclient.setVar(data.getString("entity_id"),data.);
                /*String eid = data.getString("entity_id");
                for (String key:data.keySet()) {
                    if (!key.equalsIgnoreCase("entity_id")) {
                        haclient.setVar("device_" +eid+"_"+key,data.get(key));
                    }
                }*/

            }
        });
    }

    private static void checkValue(String uuid, String clz, JSONObject inKosmos, JSONObject inHA) {

        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }
        Assert.assertNotNull(d, "device " + uuid + " could not be found!");
        if (inKosmos != null) {
            for (String key : inKosmos.keySet()) {
                Assert.assertTrue(CommonBase.waitForValue(d, key, inKosmos.get(key), 10000), "device " + uuid + " did not have correct value for " + key + " in KosmoS");
            }
        }
        if (inHA != null) {
            for (String key : inHA.keySet()) {
                String key2 = "device_" + clz + "." + uuid + "_" + key;
                if (uuid.startsWith(clz)) {
                    key2 = "device_" + uuid + "_" + key;
                }
                boolean v = haclient.waitForValue(key2, inHA.get(key), 10000);
                if (haclient.getVars().has(key2)) {
                    try {
                        if (JSONChecker.equals(haclient.getVar(key2), inHA.get(key))) {
                            return;
                        }
                    } catch (CompareException e) {
                        e.printStackTrace();
                    }
                    Assert.assertTrue(v, "device " + uuid + " did not have correct value for " + key + " in HA (" + key2 + ") " + haclient.getVar(key2) + " should have been " + inHA.get(key));
                } else {
                    Assert.assertTrue(v, "device " + uuid + " did not have correct value for " + key + " could not find state in HA (" + key2 + ")" + haclient.getVars().toString(2));
                }
            }
        }
    }

    private static void createHAClient() {
        haclient = new HomeAssistantHTTPClient("http://localhost:48123", "kosmos", "pass");

    }

    public static void reTestDevices() {
        if (SkipRetest) {
            return;
        }

        testHeater19();
        testLamp16();
        testLamp17();
        testLamp18();
        testLamp19();
        testvlight2();
    }

    public static void restartHA() {
        Assert.assertNotNull(haclient);
        CountDownLatch latch = createLatch("restartHA", 1);
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "homeassistant").put("service", "restart").put("service_data", new JSONObject()), (client, json) -> {


        });
        //haclient.disconnect();
        haclient.disconnect();
        scheduledExecutorService.schedule(() -> {
                    haclient.unstop();
                    setupConnection();
                    try {
                        haclient.waitForInit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    waitForHA();
                    latch.countDown();
                },
                120,
                TimeUnit.SECONDS);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void set(Device d, String key, Object value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", d.getUniqueID());

        jsonObject.put(key, value);
        //set the state via the rest API

        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set? " + new String(response.getContent(), StandardCharsets.UTF_8));
        Assert.assertTrue(CommonBase.waitForValue(d, key, value, 10000), "device did not have correct state");

    }

    private static void setValueAndCheck(String uuid, String clz, JSONObject state, JSONObject toVerify) {
        try {
            CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        JSONObject json = new JSONObject(state.toMap());
        json.put("id", uuid);
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", json);
        //check if the result was accepted
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        checkValue(uuid, clz, state, toVerify);

    }

    private static void parseHAState(HomeAssistantClient client, JSONObject new_state, String entity_id) {
        JSONObject attributes = new_state.optJSONObject("attributes");
        if (attributes != null) {
            //iterate over all keys and and save the new value
            for (String key : attributes.keySet()) {
                client.setVar(String.format("device_%s_%s", entity_id, key), attributes.get(key));
            }
        }
        //save the state
        if (new_state.has("state")) {
            client.setVar(String.format("device_%s_state", entity_id), new_state.get("state"));
        }
    }

    public static void setupConnection() {
        haclient.connect();
        var ref = new Object() {
            ScheduledFuture<?> t = null;
        };
        ref.t = scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (haclient.getWebSocket().isAuthed()) {
                ref.t.cancel(false);


            }


        }, 1000, 100, TimeUnit.MILLISECONDS);
        try {
            ref.t.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (CancellationException e) {
            //this is expected!
            //e.printStackTrace();
        }
        haclient.sendCommand(new JSONObject().put("type", "config/core/update").put("location_name", "BAALL").put("latitude", 53.105944629013486).put("longitude", 8.85490268468857).put("elevation", 0).put("unit_system", "metric").put("time_zone", "Europe/Berlin"), (client, json) -> {
            //System.out.println(json.toString());
            Assert.assertTrue(json.optBoolean("success", false), "could not set location");
            client.setVar("teststep", 3);

        });

        Assert.assertTrue(haclient.waitForValue("teststep", 3, 10000), "did not set location");
        haclient.sendCommand(new JSONObject().put("type", "analytics/preferences").put("preferences", new JSONObject()), (client, json) -> {
            //System.out.println(json.toString());
            Assert.assertTrue(json.optBoolean("success", false), "could not subscribe to core config");
            client.setVar("teststep", 4);


        });
        Assert.assertTrue(haclient.waitForValue("teststep", 3, 10000), "did not set location");
        haclient.sendCommand(new JSONObject().put("type", "get_states"), (client, json) -> {
            //System.out.println(json.toString());

            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not get states");
                JSONArray arr = json.getJSONArray("result");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonObject = arr.getJSONObject(i);
                    parseHAState(client, jsonObject.getJSONObject("attributes"), jsonObject.getString("entity_id"));
                }
                client.initLatch.countDown();
            }

        });
        Assert.assertTrue(haclient.waitForValue("teststep", 4, 10000), "did not set preferences");
        haclient.sendCommand(new JSONObject().put("type", "subscribe_events").put("event_type", "state_changed"), (client, json) -> {
            if (json.has("success")) {

                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }

            if (json.has("type") && json.getString("type").equals("event")) {
                logger.info("received event: {}", json.toString(2));
                //check if its an event - always true
                JSONObject event = json.getJSONObject("event");
                if (event != null) {
                    //should also be always true
                    JSONObject data = event.getJSONObject("data");
                    if (data != null) {
                        //could ne null
                        JSONObject new_state = data.getJSONObject("new_state");
                        if (new_state != null) {
                            //could also be null
                            parseHAState(client, new_state, data.getString("entity_id"));
                        }
                    }
                }
                //
            }


        });

    }

    @Test(dependsOnMethods = {"setupHA"})
    public static void testHeater19() {

        String uuid = "heater19";
        String clz = "climate";

        //checkValue(uuid, clz, new JSONObject().put("heatingTemperatureSetting", 15.5), new JSONObject().put("heatingtemperaturesetting_temperature", 15.5));
        setValueAndCheck(uuid, clz, new JSONObject().put("heatingTemperatureSetting", 20), new JSONObject().put("heatingtemperaturesetting_temperature", 20));
        setValueAndCheck(uuid, clz, new JSONObject().put("heatingTemperatureSetting", 21.5), new JSONObject().put("heatingtemperaturesetting_temperature", 21.5));
        callServiceOnHA(clz, "set_temperature", new JSONObject().put("entity_id", clz + "." + uuid + "_heatingtemperaturesetting").put("temperature", 23));
        checkValue(uuid, clz, new JSONObject().put("heatingTemperatureSetting", 23), null);
        callServiceOnHA(clz, "set_temperature", new JSONObject().put("entity_id", clz + "." + uuid + "_heatingtemperaturesetting").put("temperature", 23.5));
        checkValue(uuid, clz, new JSONObject().put("heatingTemperatureSetting", 23.5), null);
    }

    @Test(dependsOnMethods = {"setupHA"})
    public static void testLamp16() {

        String uuid = "lamp16";
        String clz = "light";
        setValueAndCheck(uuid, clz, new JSONObject().put("on", false), new JSONObject().put("state", "off"));
        setValueAndCheck(uuid, clz, new JSONObject().put("on", true), new JSONObject().put("state", "on"));
        callServiceOnHA(clz, "turn_off", new JSONObject().put("entity_id", clz + "." + uuid));
        /*haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", clz).put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", clz+"."+uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });*/
        checkValue(uuid, clz, new JSONObject().put("on", false), null);
        /*haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", clz).put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", clz+"."+uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not call service turn_on");
            }
        });*/
        callServiceOnHA(clz, "turn_on", new JSONObject().put("entity_id", clz + "." + uuid));
        checkValue(uuid, clz, new JSONObject().put("on", true), new JSONObject().put("state", "on"));
    }

    @Test(dependsOnMethods = {"setupHA"})
    public static void testLamp17() {
        String uuid = "lamp17";
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        set(d, "on", true);
        set(d, "on", false);
        set(d, "on", true);


        //wait for lamp17 to be updated via HA websocket
        Assert.assertTrue(haclient.waitForValue("device_light.lamp17_state", "on", 10000), "device was not propagated via HA");

        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "lamp17");
        //we want the lamp to turn off
        jsonObject.put("on", false);
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_light.lamp17_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("on", true);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_light.lamp17_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", "light.lamp17")), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light.lamp17_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", "light.lamp17")), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        //Assert.assertTrue(haclient.waitForValue("device_light.lamp17_state", "on", 10000), "device did not have correct state");

        logger.info("HAClientTest SUCCESS!!");

    }

    @Test(dependsOnMethods = {"setupHA"})
    public static void testLamp18() {

        /*String uuid = "lamp18";
        Device d = CommonBase.controller.getDevice(uuid);
        Assert.assertNotNull(d, "device could not be found again");
        
        //wait for lamp17 to be updated via HA websocket
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device was not propagated via HA");
        
        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("on", false);
        //set the state via the rest API
        ContentResponse response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");
        
        //turn it on again
        jsonObject.put("on", true);
        jsonObject.put("dimmingLevel", 50);
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        Assert.assertTrue(CommonBase.waitForValue(d, "dimmingLevel", 50, 1000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_brightness", 127, 10000), "device did not have correct dimmingLevel");
        jsonObject.put("dimmingLevel", 100);
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        Assert.assertTrue(CommonBase.waitForValue(d, "dimmingLevel", 100, 1000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_brightness", 254, 10000), "device did not have correct dimmingLevel");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", "light." + uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");
        
        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", "light." + uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
        
        System.out.println("HAClientTest SUCCESS!!");
        */

        String uuid = "lamp18";
        String clz = "light";
        setValueAndCheck(uuid, clz, new JSONObject().put("on", false), new JSONObject().put("state", "off"));
        setValueAndCheck(uuid, clz, new JSONObject().put("on", true), new JSONObject().put("state", "on"));
        setValueAndCheck(uuid, clz, new JSONObject().put("on", false), new JSONObject().put("state", "off"));
        //TODO: 254 is the maximum due to a rounding problem in the HA Integration, but okay we'll ignore that for now
        setValueAndCheck(uuid, clz, new JSONObject().put("on", true).put("dimmingLevel", 100), new JSONObject().put("state", "on").put("brightness", 254));
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", clz + "." + uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        checkValue(uuid, clz, new JSONObject().put("on", false), null);
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", clz + "." + uuid).put("brightness", 255)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        checkValue(uuid, clz, new JSONObject().put("on", true).put("dimmingLevel", 100), null);
    }

    /**
     * tests if a lamp could be added dynamically to HA and be controlled directly
     */
    @Test(dependsOnMethods = {"createLamp19"})
    public static void testLamp19() {
        String uuid = "hatest_lamp19";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        set(d, "on", false);
        set(d, "on", true);

        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device was not propagated via HA" + haclient.getVars());

        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("on", false);
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("on", true);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", "light." + uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "on", false, 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", "light." + uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        Assert.assertTrue(CommonBase.waitForValue(d, "on", true, 10000), "device did not have correct state");
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
    }

    @Test(dependsOnMethods = {"setupHA"})
    public static void testvlight2() {
        if (true) return;
        String uuid = "virtual_vlight2";
        String clz = "light";
        String iuuid = clz + "." + uuid;


        callServiceOnHA(clz, "turn_on", new JSONObject().put("entity_id", clz + "." + uuid));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }


        CommonBase.waitForValue(d, "state", "on", 10000);

        callServiceOnHA(clz, "turn_off", new JSONObject().put("entity_id", clz + "." + uuid));
        //

        try {
            d = CommonBase.controller.getDevice(uuid);
            CommonBase.waitForValue(d, "state", "off", 10000);
            checkValue(iuuid, clz, new JSONObject().put("state", "off"), new JSONObject().put("state", "off"));
            callServiceOnHA(clz, "turn_on", new JSONObject().put("entity_id", clz + "." + uuid));
            checkValue(iuuid, clz, new JSONObject().put("state", "on"), new JSONObject().put("state", "on"));
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }


    }

    @Test(dependsOnMethods = {"testConsumers"})
    public static void waitForHA() {
        CountDownLatch latch = createLatch("waitFoHA", 2);
        long started = System.currentTimeMillis();

        try {
            if (haclient == null) {
                createHAClient();
            }
            Request request = haclient.newRequest("http://localhost:48123/");
            var ref = new Object() {
                ScheduledFuture<?> t = null;
            };


            ref.t = scheduledExecutorService.scheduleAtFixedRate(() -> {

                long running = System.currentTimeMillis() - started;
                if (running > 300000L) {
                    Assert.fail("could not connect to HA after waiting for 5minutes");
                }
                logger.info("waiting for HA since {}", running);
                try {
                    request.method(HttpMethod.GET);
                    ContentResponse response = request.send();
                    if (response.getStatus() == 200) {
                        logger.info("reached ha after {}", running);

                        ref.t.cancel(false);
                        latch.countDown();
                    } else {
                        logger.info("wrong status from HA: {}", response.getStatus());
                    }
                } catch (TimeoutException | ExecutionException timeoutException) {
                    timeoutException.printStackTrace();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }


            }, 10000, 1000, TimeUnit.MILLISECONDS);

            try {
                ref.t.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (CancellationException e) {
                //this is expected!
                //e.printStackTrace();
            }

            logger.info("HA is reachable, lets wait another {}s for it to start", WAITTIME);

            scheduledExecutorService.schedule(() -> {
                        latch.countDown();


                    },
                    10,
                    TimeUnit.SECONDS).get();
            latch.await();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //Assert.fail("could not connect to HA after waiting for 5minutes");

    }

    private void addDeviceToKosmos(String uuid, String schema, JSONObject state) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("schema", schema);
        if (state == null) {
            state = new JSONObject();
        }
        jsonObject.put("state", state);
        addDeviceToKosmos(jsonObject);

    }

    private void addDeviceToKosmos(JSONObject jsonObject) {
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/add", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Add not successful?");

        Device d = null;
        try {
            d = CommonBase.controller.getDevice(jsonObject.getString("uuid"));
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + jsonObject.getString("uuid") + " could not be found!");
        }


        JSONObject state = jsonObject.getJSONObject("state");
        for (String key : state.keySet()) {
            String dkey = d.get(key).toString();
            String skey = state.get(key).toString();
            Assert.assertEquals(dkey, skey, "Key " + key + " for device " + jsonObject.getString("uuid") + " differs!\n\"" + (dkey) + "\" vs \"" + (skey) + "\"");
        }

    }

    private void addTestDevicesToKosmos() {
        /*JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", "lamp17");
        jsonObject.put("schema", "https://kosmos-lab.de/schema/Lamp.json");
    
        jsonObject.put("state", new JSONObject().put("on", true));
        ContentResponse response = CommonBase.clientAdmin.postJSONObject2("/device/add", jsonObject);
        Assert.assertEquals(response.getStatus(), 204, "Add not successful?");
        Device d = CommonBase.controller.getDevice("lamp17");
        Assert.assertNotNull(d, "device could not be found again");
        */
        addDeviceToKosmos("lamp16", "https://kosmos-lab.de/schema/Lamp.json", new JSONObject().put("on", true));

        addDeviceToKosmos("lamp17", "https://kosmos-lab.de/schema/Lamp.json", new JSONObject().put("on", true));
        addDeviceToKosmos("lamp18", "https://kosmos-lab.de/schema/DimmableLamp.json", new JSONObject().put("on", true).put("dimmingLevel", 10));
        addDeviceToKosmos("heater19", "https://kosmos-lab.de/schema/Heater.json", new JSONObject().put("heatingTemperatureSetting", 15.5));
    }

    @AfterClass
    public void cleanup() {


        if (haclient != null) {

            //restartHA();

            //reTestDevices();
            try {
                haclient.disconnect();
                haclient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Test(dependsOnMethods = {"setupHA"})
    public void createDynamicsensor() {
        String uuid = "hatest_sensor141";
        uuid = uuid.toLowerCase();
        try {
            CommonBase.controller.getDevice(uuid);
            Assert.fail("device already existed");
        } catch (DeviceNotFoundException ex) {

        }

        addDeviceToKosmos(uuid, "https://kosmos-lab.de/schema/MultiSensor.json", new JSONObject().put("humidityLevel", 10));

        try {
            CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

    }

    @Test(dependsOnMethods = {"setupHA"})
    public void createLamp19() {
        String uuid = "hatest_lamp19";
        uuid = uuid.toLowerCase();
        try {
            CommonBase.controller.getDevice(uuid);
            Assert.fail("device already existed");
        } catch (DeviceNotFoundException ex) {

        }

        addDeviceToKosmos(uuid, "https://kosmos-lab.de/schema/Lamp.json", new JSONObject().put("on", true));
    }

    @Test(dependsOnMethods = {"setupHA"})
    public void testKNXRGBLamp() {


        String uuid = "light.RGB_light";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        set(d, "state", "OFF");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device was not propagated via HA" + haclient.getVars());
        set(d, "state", "ON");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device was not propagated via HA" + haclient.getVars());


        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("state", "off");
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("state", "on");
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });

        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        jsonObject.put("state", "on");
        jsonObject.put("rgb_color", new JSONArray().put(255).put(0).put(255));
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(255).put(0).put(255), 10000), "device did not have correct rgb_color");
        jsonObject.put("state", "on");
        jsonObject.put("rgb_color", new JSONArray().put(0).put(255).put(0));
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(0).put(255).put(0), 10000), "device did not have correct rgb_color");

        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");

    }

    @Test(dependsOnMethods = {"setupHA"})
    public void testKNXRGBWLamp() throws InterruptedException {


        String uuid = "light.RGBW_light";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        set(d, "state", "OFF");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device was not propagated via HA" + haclient.getVars());
        set(d, "state", "ON");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device was not propagated via HA" + haclient.getVars());


        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("state", "off");
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("state", "on");
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });

        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        jsonObject.put("state", "on");
        jsonObject.put("rgb_color", new JSONArray().put(255).put(0).put(255));
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        //Assert.assertTrue(CommonBase.waitForValue(d, "hs_color", new JSONArray().put(300).put(100), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(255).put(0).put(255), 10000), "device did not have correct rgb_color");
        jsonObject.put("state", "on");
        jsonObject.remove("rgb_color");
        jsonObject.put("rgbw_color", new JSONArray().put(0).put(255).put(0).put(0));

        Thread.sleep(1000);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response, "not set?");
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        Assert.assertTrue(CommonBase.waitForValue(d, "rgbw_color", new JSONArray().put(0).put(255).put(0).put(0), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(0).put(255).put(0), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgbw_color", new JSONArray().put(0).put(255).put(0).put(0), 10000), "device did not have correct state");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgb_color", new JSONArray().put(0).put(255).put(0), 10000), "device did not have correct state");
        jsonObject.put("rgbw_color", "[255,255,0,0]");
        Thread.sleep(1000);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response, "not set?");
        Assert.assertEquals(response.getStatus(), 200, response.getContentAsString());
        Assert.assertTrue(CommonBase.waitForValue(d, "rgbw_color", new JSONArray().put(255).put(255).put(0).put(0), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgbw_color", new JSONArray().put(255).put(255).put(0).put(0), 10000), "device did not have correct state");
        Thread.sleep(1000);
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(255).put(255).put(0), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgb_color", new JSONArray().put(255).put(255).put(0), 10000), "device did not have correct state");
        jsonObject.remove("rgbw_color");

        jsonObject.put("rgb_color", "[0,0,255]");
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response, "not set?");
        Assert.assertEquals(response.getStatus(), 200, response.getContentAsString());
        Assert.assertTrue(CommonBase.waitForValue(d, "rgb_color", new JSONArray().put(0).put(0).put(255), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgb_color", new JSONArray().put(0).put(0).put(255), 10000), "device did not have correct state");
        Thread.sleep(1000);

        Assert.assertTrue(CommonBase.waitForValue(d, "rgbw_color", new JSONArray().put(0).put(0).put(255).put(0), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_rgbw_color", new JSONArray().put(0).put(0).put(255).put(0), 10000), "device did not have correct state");
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");

    }


    @Test(dependsOnMethods = {"setupHA"})
    public void testKNXCCT_Light() {


        String uuid = "light.cct_light";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }

        set(d, "state", "OFF");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device was not propagated via HA" + haclient.getVars());
        set(d, "state", "ON");
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device was not propagated via HA" + haclient.getVars());


        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("state", "off");
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("state", "on");
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id", uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });

        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        jsonObject.put("state", "on");
        jsonObject.put("color_temp", 162);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        //Assert.assertTrue(CommonBase.waitForValue(d, "hs_color", new JSONArray().put(300).put(100), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(CommonBase.waitForValue(d, "color_temp", 162, 10000), "device did not have correct color_temp");
        jsonObject.put("state", "on");

        jsonObject.put("color_temp", 300);
        response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response, "not set?");
        Assert.assertEquals(response.getStatus(), 200, "not set?");

        Assert.assertTrue(CommonBase.waitForValue(d, "color_temp", 300, 10000), "device did not have correct rgb_color");

        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_color_temp", 300, 10000), "device did not have correct state");

        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");

    }

    @Test(dependsOnMethods = {"setupHA"})
    public void testKNXCover() {


        String uuid = "cover.kitchen_shutter";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }
        set(d, "state", "open");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        set(d, "state", "close");
        //Assert.assertTrue(haclient.waitForValue("device_"+uuid + "_state", "closing", 5000), "closing was not propagated via HA" + haclient.getVars());
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "closed", 30000), "closed was not propagated via HA" + haclient.getVars());
        //set(d, "state", "open");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("state", "open");
        //set the state via the rest API
        ContentResponse response = CommonBase.httpClientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "opening", 5000), "opening was not propagated via HA" + haclient.getVars());
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "open", 30000), "opened was not propagated via HA" + haclient.getVars());

/*
        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("state", "off");
        //set the state via the rest API
        ContentResponse response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);

        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_"+uuid + "_state", "off", 10000), "device did not have correct state");

        //turn it on again
        jsonObject.put("state","on");
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_state", "on", 10000), "device did not have correct state");
        //turn off via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_off").put("service_data", new JSONObject().put("entity_id",  uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });
        //check if it was changed locally
        Assert.assertTrue(CommonBase.waitForValue(d, "state", "off", 10000), "device did not have correct state");
        //check if HA did change its value (the turn_on actually does NOT change the value, it waits for a response from KosmoS to verify it is actually accepted)
        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "off", 10000), "device did not have correct state");

        //turn ON via HA
        haclient.sendCommand(new JSONObject().put("type", "call_service").put("domain", "light").put("service", "turn_on").put("service_data", new JSONObject().put("entity_id",  uuid)), (client, json) -> {
            if (json.has("success")) {
                Assert.assertTrue(json.getBoolean("success"), "could not subscribe to events");
            }
        });

        Assert.assertTrue(CommonBase.waitForValue(d, "state", "on", 10000), "device did not have correct state");
        jsonObject.put("state","on");
        jsonObject.put("color_temp",162);
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        //Assert.assertTrue(CommonBase.waitForValue(d, "hs_color", new JSONArray().put(300).put(100), 10000), "device did not have correct rgb_color");
        Assert.assertTrue(CommonBase.waitForValue(d, "color_temp", 162, 10000), "device did not have correct color_temp");
        jsonObject.put("state","on");

        jsonObject.put("color_temp",300);
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertNotNull(response, "not set?");
        Assert.assertEquals(response.getStatus(), 200, "not set?");

        Assert.assertTrue(CommonBase.waitForValue(d, "color_temp", 300, 10000), "device did not have correct rgb_color");

        Assert.assertTrue(haclient.waitForValue("device_" + uuid + "_color_temp", 300, 10000), "device did not have correct state");

        //Assert.assertTrue(haclient.waitForValue("device_light." + uuid + "_state", "on", 10000), "device did not have correct state");
*/
    }

    private void setAndCheckInHA(Device d, String key, String domain, Object value) {

        set(d, key, value);

        Assert.assertTrue(haclient.waitForValue("device_" + domain + "." + d.getUniqueID() + "_" + key.toLowerCase() + "_state", value, 10000), "device_" + domain + "." + d.getUniqueID() + "_" + key.toLowerCase() + "_state did not have correct state (" + value + ")\n" + haclient.getVars().toString(2));
    }


    private static final HashMap<String, CountDownLatch> latches = new HashMap<>();

    public static CountDownLatch createLatch(String name, int number) {
        CountDownLatch latch = new CountDownLatch(number);
        latches.put(name, latch);
        return latch;
    }

    public void countDownLatch(String name) throws InvalidKeyException {
        CountDownLatch latch = latches.get(name);
        if (latch != null) {
            latch.countDown();
        } else {
            throw new InvalidKeyException("No such key found " + name);
        }
    }

    public void waitForLatch(String name) throws InterruptedException {
        CountDownLatch latch = latches.get(name);
        if (latch != null) {
            latch.await();
        }
    }

    public static boolean checkIfReachable(String host) {

        Request request = haclient.newRequest("http://" + host + ":18083/");
        try {
            request.method(HttpMethod.GET);

            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                //we can see ourselves, so assume docker will too


                logger.info("found kosmos host: {}", host);
                return true;
            } else {
                logger.info("response status: {}", (response.getStatus()));
            }
        } catch (TimeoutException timeoutException) {
            timeoutException.printStackTrace();
        } catch (ExecutionException executionException) {
            executionException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Test(dependsOnMethods = {"waitForHA"})
    public void setupHA() throws UnknownHostException, SocketException, InterruptedException {
        JSONObject obj = new JSONObject().put("client_id", haclient.getBase()).put("name", "kosmos").put("username", haclient.getUser()).put("password", haclient.getPass()).put("language", "en");
        JSONObject result = haclient.postJSON("/api/onboarding/users", obj);
        Assert.assertTrue(result.has("auth_code"), "could not add user");

        HashMap<String, Object> p = new HashMap<String, Object>();
        p.put("client_id", haclient.getBase());
        p.put("code", result.getString("auth_code"));
        p.put("grant_type", "authorization_code");
        result = new JSONObject(haclient.post("/auth/token", p));

        Assert.assertTrue(result.has("access_token"), "no access token!");
        haclient.setToken(result.getString("access_token"));
        CommonBase.startIfNeeded();

        setupConnection();
        haclient.waitForInit();

        addTestDevicesToKosmos();
        CountDownLatch latch = createLatch("setupHA", 1);
        scheduledExecutorService.schedule(() -> {
                    logger.info("adding flow to HA");
                    JSONObject jsonResult = haclient.postJSON("/api/config/config_entries/flow", new JSONObject().put("handler", "kosmos").put("show_advanced_options", true));
                    logger.info("got flow result back {}", jsonResult);

                    Assert.assertNotNull(jsonResult);
                    Assert.assertTrue(jsonResult.has("flow_id"));
                    String flow_id = jsonResult.getString("flow_id");
                    logger.info("got flow id {}", flow_id);

                    Enumeration networkInterfaces = null;
                    try {
                        networkInterfaces = NetworkInterface.getNetworkInterfaces();
                        String host = null;
                        if (!Utils.skip_docker_creation()) {
                            logger.info("not in docker ci");
                            //check default Docker Host IP first
                            String h = "172.17.0.1";
                            if (checkIfReachable(h)) {
                                host = h;
                            }
                            if (host == null) {
                                networkloop:
                                while (networkInterfaces.hasMoreElements()) {
                                    //iterate over all network interfaces
                                    NetworkInterface n = (NetworkInterface) networkInterfaces.nextElement();
                                    Enumeration ee = n.getInetAddresses();
                                    //get all ips
                                    while (ee.hasMoreElements()) {
                                        InetAddress i = (InetAddress) ee.nextElement();
                                        h = i.getHostAddress();
                                        //skip v6 and localhost
                                        if (h.contains(".") && !h.equals("127.0.0.1")) {
                                            //System.out.println(h);
                                            //haclient.post("http://"+i.getHostAddress()+":18083",new HashMap<>());
                                            //try to see if we can reach "ourself"
                                            if (checkIfReachable(h)) {
                                                host = h;
                                                break networkloop;
                                            }
                                        }

                                    }
                                }
                            }
                        } else {
                            logger.info("IN docker ci");
                            host = "127.0.0.1";
                        }
                        Assert.assertNotNull(host, "could not find a connectable host!");
                        //send KosmoS config to HA
                        logger.info("sending final flow to HA");
                        jsonResult = haclient.postJSON("/api/config/config_entries/flow/" + flow_id, new JSONObject().put("host", "http://" + host + ":18083").put("username", "ha").put("password", "pass"));
                        jsonResult = haclient.postJSON("/api/config/config_entries/flow", new JSONObject().put("handler", "knx").put("show_advanced_options", false));
                        logger.info("got flow result for knx back {}", jsonResult);

                        flow_id = jsonResult.getString("flow_id");
                        haclient.postJSON("/api/config/config_entries/flow/" + flow_id, new JSONObject().put("connection_type", "routing"));
                        haclient.postJSON("/api/config/config_entries/flow/" + flow_id, new JSONObject().put("individual_address", "15.15.250").put("multicast_group", "224.0.23.12").put("multicast_port", "3671"));

                        latch.countDown();


                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                },
                10,
                TimeUnit.SECONDS);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConsumers() {
        logger.info("Profile id: {}", System.getProperty("profileId"));
        HomeAssistantClient dummyClient = new HomeAssistantClient(null);

        dummyClient.addConsumer(2, (client, json) -> {
            client.setVar("hatest1", 17);
        });
        dummyClient.addConsumer(3, (client, json) -> {
            client.setVar("hatest2", json.getString("event"));
        });


        dummyClient.onMessageReceived(new JSONObject().put("id", 2).toString());
        dummyClient.waitForValue("hatest1", 17, 1000);
        Assert.assertEquals(dummyClient.getVar("hatest1"), 17, "parser did not set the correct value?");
        dummyClient.onMessageReceived(new JSONObject().put("id", 3).put("event", "test").toString());
        dummyClient.waitForValue("hatest1", 12, 1000);
        dummyClient.setVar("hatest1", 12);
        Assert.assertNotEquals(dummyClient.getVar("hatest1"), 17, "parser did change the value back");
        Assert.assertEquals(dummyClient.getVar("hatest2"), "test", "parser did not set the correct value?");
        dummyClient.waitForValue("hatest2", "test", 1000);
    }

    @Test(dependsOnMethods = {"createDynamicsensor"})
    public void testDynamicsensor() {


        String uuid = "hatest_sensor141";
        uuid = uuid.toLowerCase();
        Device d = null;
        try {
            d = CommonBase.controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {
            Assert.fail("device " + uuid + " could not be found!");
        }
        //test the initial states
        //setAndCheckInHA(d, "humidityLevel", "sensor", 10);
        //setAndCheckInHA(d, "humidityLevel", "sensor", 50);
        //setAndCheckInHA(d, "humidityLevel", "sensor", 80);


        //add a new state and check if it appears
        setAndCheckInHA(d, "currentEnvironmentTemperature", "sensor", 15);
        setAndCheckInHA(d, "currentEnvironmentTemperature", "sensor", 25);


        //add a new binary state and check if it also appears
        setAndCheckInHA(d, "fallDetected", "binary_sensor", false);
        setAndCheckInHA(d, "fallDetected", "binary_sensor", true);

        /*Assert.assertTrue(haclient.waitForValue("device_sensor."+uuid+"_humiditylevel", 10, 10000), "device was not propagated via HA"+haclient.getVars());
        
        //lets start constructing our json to update the state
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("humidityLevel", 20);
        //set the state via the rest API
        ContentResponse response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "humidityLevel", 20, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_sensor."+uuid+"_humiditylevel", 20, 10000), "device did not have correct state");
        
        //turn it on again
        jsonObject.put("humidityLevel", 30);
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //is it changed locally?
        Assert.assertTrue(CommonBase.waitForValue(d, "humidityLevel", 30, 10000), "device did not have correct state");
        //wait for HA to propagate
        Assert.assertTrue(haclient.waitForValue("device_sensor."+uuid+"_humiditylevel", "on", 10000), "device did not have correct state");
        //turn off via HA
    
    
    
        jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("currentEnvironmentTemperature", 25);
        //set the state via the rest API
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
    
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "currentEnvironmentTemperature", 25, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_sensor."+uuid+"_currentenvironmenttemperature", 25, 10000), "device did not have correct state");
    
    
    
        jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("currentEnvironmentTemperature", 15);
        //set the state via the rest API
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
    
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "currentEnvironmentTemperature", 15, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_sensor."+uuid+"_currentenvironmenttemperature", 15, 10000), "device did not have correct state");
    
    
        jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        //we want the lamp to turn off
        jsonObject.put("fallDetected", true);
        //set the state via the rest API
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
    
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "fallDetected", true, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_binary_sensor."+uuid+"_falldetected", true, 10000), "device did not have correct state");
        
        
        jsonObject = new JSONObject();
        jsonObject.put("id", uuid);
        
        jsonObject.put("fallDetected", false);
        //set the state via the rest API
        response = CommonBase.clientAdmin.postJSONObject2("/device/set", jsonObject);
    
        //check if the result was accepted
        Assert.assertEquals(response.getStatus(), 200, "not set?");
        //check if our local state changed
        Assert.assertTrue(CommonBase.waitForValue(d, "fallDetected", false, 10000), "device did not have correct state");
        //check if the HA state has changed
        Assert.assertTrue(haclient.waitForValue("device_binary_sensor."+uuid+"_falldetected", false, 10000), "device did not have correct state");*/
    }

}


