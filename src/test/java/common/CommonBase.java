package common;

import CI.HTTP.TestDevice;
import CI.HTTP.TestGroup;

import Integration.HAClientTest;
import de.kosmos_lab.platform.exceptions.GroupNotFoundException;
import de.kosmos_lab.platform.utils.KosmoSHelper;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.platform.persistence.Constants.RunMode;
import de.kosmos_lab.utils.FileUtils;
import de.kosmos_lab.utils.exceptions.CompareException;
import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.platform.client.KosmoSClient;
import de.kosmos_lab.platform.KosmoSController;
import de.kosmos_lab.utils.JSONChecker;
import de.kosmos_lab.utils.StringFunctions;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.KosmoSUser;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
public class CommonBase {
    final public static ConcurrentHashMap<String, JSONObject> jsonCache = new ConcurrentHashMap<>();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("Test");
    public static String pass;

    public static KosmoSController controller;
    public static KosmoSClient clientAdmin;
    public static KosmoSClient clientUser;
    public static KosmoSClient clientUser2;
    public static KosmoSClient clientUser3;

    public static KosmoSClient clientFakeUser;
    public static String baseUrl = "";
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static KosmoSClient clientha;


    @SuppressFBWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    @BeforeSuite
    public static void prepare() {
        String TEST_HOST = KosmoSHelper.getEnv("TEST_HOST");
        logger.info("Test host:{}", TEST_HOST);
        if (TEST_HOST == null || TEST_HOST.length() == 0) {

            File testdb = KosmoSController.getFile("db/db.sqlite", RunMode.TEST);
            if (testdb.exists()) {
                //Assert.assertTrue(testdb.delete(),"could not delete old test db!!");
                logger.info("deleting old DB {}",testdb);
                if (!testdb.delete()) {
                    // we don't actually care about the return value, the next loop will get it deleted
                    // this might happen if we start the test again while the previous one is still shutting down
                }

            }
            if (testdb.exists()) {
                for (int i = 0; i < 12; i++) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {

                        logger.error("could not sleep!", e);
                    }
                    if (testdb.delete()) {
                        break;
                    }

                }
            }
            if (testdb.exists()) {
                Assert.fail("could not delete old test db!!");
            }
            for (String d : new String[]{"rules/rules", "gestures", "db"}) {
                File dir = KosmoSController.getFile(d, RunMode.TEST);
                if (dir.exists()) {
                    for (File f : dir.listFiles()) {
                        logger.info("deleting old file {}",f);
                        if (!f.delete()) {

                            Assert.fail(String.format("could not delete old file %s", f));
                        }
                    }
                } else {
                    dir.mkdirs();
                }
            }


            pass = StringFunctions.generateRandomKey();
            File testConf = KosmoSController.getFile("config.json", RunMode.TEST);
            if (testConf.exists()) {
                if(!testConf.delete()) {
                    Assert.fail(String.format("could not delete old test config %s", testConf));

                }
            }
            setup();

            try {

                clientAdmin = new KosmoSClient(baseUrl, "admin", StringFunctions.generateRandomKey());
                clientUser = new KosmoSClient(baseUrl, "user", StringFunctions.generateRandomKey());
                clientUser2 = new KosmoSClient(baseUrl, "user2", StringFunctions.generateRandomKey());
                clientUser3 = new KosmoSClient(baseUrl, "user3", StringFunctions.generateRandomKey());

                clientFakeUser = new KosmoSClient(baseUrl, "fakeuser2", "test");
                FileUtils.writeToFile(KosmoSController.getFile("users.json", RunMode.TEST), new JSONObject().put("admin", clientAdmin.getPassword()).put("user", clientUser.getPassword()).put("user2", clientUser2.getPassword()).put("user3", clientUser3.getPassword()).toString());

            } catch (Exception e) {
                logger.error("could not create users and write the information to a seperate file!", e);
            }
            String pass = clientAdmin.getPassword();
            Assert.assertNotNull(pass);
            controller.addUser("admin", pass, 1000);
            controller.addUser("ha", "pass", 1);
        } else {
            logger.info("using services for kosmos");
            baseUrl = "http://" + TEST_HOST;
            try {
                clientFakeUser = new KosmoSClient(baseUrl, "fakeuser2", "test");
            } catch (Exception e) {
                logger.error("could not login as fake user!", e);
            }

            String USERS = KosmoSHelper.getEnv("USERS");
            if (USERS != null) {
                logger.info("found users! {}", USERS);
                try {
                    JSONArray arr = new JSONArray(USERS);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String username = obj.getString("username");
                        logger.info("found user: {}", username);


                        if (username.equals("admin")) {
                            try {
                                clientAdmin = new KosmoSClient(baseUrl, username, obj.getString("password"));
                            } catch (Exception e) {
                                logger.error("could not login as admin!", e);
                            }
                        }
                        if (username.equals("user")) {
                            try {
                                clientUser = new KosmoSClient(baseUrl, username, obj.getString("password"));
                            } catch (Exception e) {
                                logger.error("could not login as user!", e);
                                //
                            }
                        }
                        if (username.equals("user2")) {

                            try {

                                clientUser2 = new KosmoSClient(baseUrl, username, obj.getString("password"));
                            } catch (Exception e) {
                                logger.error("could not login as user2!", e);
                            }
                        }
                    }
                } catch (JSONException ex) {
                    logger.error("could not parse JSON!", ex);
                }

            }


        }


    }

    public static void restart() {
        controller.stop();
        setup();
    }

    public static void setup() {        JSONObject config = new JSONObject();

        File testConf = KosmoSController.getFile("config.json", RunMode.TEST);
        if (testConf.exists()) {
            try {
                config = new JSONObject(KosmosFileUtils.readFile(testConf));
            } catch (Exception ex ) {
                ex.printStackTrace();
            }
        }
        int port = 18083;
        config.put("webserver", new JSONObject().put("port", port));

        baseUrl = "http://localhost:" + port;


        config.put("mqtt", new JSONObject().put("port", 1884));
        config.put("sql", new JSONObject().put("url", String.format("jdbc:sqlite:%s", KosmoSController.getFile("db/db.sqlite", RunMode.TEST))));
        KosmosFileUtils.writeToFile(testConf, config.toString(2));

        controller = new KosmoSController(testConf, RunMode.TEST);

    }

    public static void startIfNeeded() {
        if (controller == null) {
            prepare();
        }

    }

    /**
     * this method is used to give the system some time to react to an order via a slow..ish medium. It checks the value
     * every 500ms and returns true as soon as it changed, or false if it never changed and the timeout was reached
     *
     * @param device   the JSONObject to "monitor"
     * @param key      the key to watch
     * @param expected the expected value
     * @param waittime waittime in ms
     *
     * @return
     */
    public static boolean waitForValue(JSONObject device, String key, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        CompareException laste = null;
        while (true) {
            try {
                if (JSONChecker.checkValue(device, key, expected)) {
                    return true;
                }
            } catch (CompareException e) {
                //dont spam the log for now
                laste = e;
            } catch (Exception e) {
                //e.printStackTrace();
                logger.warn("Exception while comparing {} to {}: {}", key, expected, e.getMessage(),e);
            }
            logger.info(device.toString());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("could not sleep!", e);
            }
            long delta = System.currentTimeMillis() - started;
            if (delta > waittime) {
                if (laste != null) {
                    logger.warn("could not match {} to {}: {}", key, expected, laste.getMessage());
                }
                return false;
            }
        }
    }

    public static boolean waitForValueJSONHttp(KosmoSClient client, String url, String key, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        while (true) {
            try {
                ContentResponse response = client.getResponse(url, HttpMethod.GET);
                if (response != null) {
                    if (response.getStatus() <= 400) {
                        JSONObject device = new JSONObject(response.getContentAsString());
                        logger.info("waitForValueJSONHttp {}", device.toString(2));
                        if (JSONChecker.checkValue(device, key, expected)) {
                            return true;
                        }
                        if (device.has("attributes")) {
                            if (JSONChecker.checkValue(device.getJSONObject("attributes"), key, expected)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("could not wait for JSON Value!", ex);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("could not sleep!", e);
            }
            long delta = System.currentTimeMillis() - started;
            if (delta > waittime) {
                return false;
            }
        }
    }

    @AfterSuite
    public void cleanup(ITestContext context) {
        //Thread.sleep(6000000000l);
    
        
        /*
        checks if the changes survive a reboot of the system.
         */
        String TEST_HOST = KosmoSHelper.getEnv("TEST_HOST");
        if (TEST_HOST == null) {

            if (context.getFailedTests().size() == 0) {
                if (KosmoSHelper.getEnvBool("DONT_STOP_TEST")) {
                    System.out.println(String.format("Test FINISHED! you can now login via %s %s and stop the server once you are done to exit the test",clientAdmin.getUserName(),clientAdmin.getPassword()));
                    while (true) {
                        try {
                            Thread.sleep(10000);
                            if (controller.isStopped()) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    setup();

                }
                else {
                    CommonBase.restart();
                }
                String name = CommonBase.clientAdmin.getUserName();
                Assert.assertNotNull(name);

                KosmoSUser admin = (KosmoSUser) CommonBase.controller.getUser(name);
                Assert.assertNotNull(admin);
                Assert.assertTrue(admin.isAdmin());
                name = CommonBase.clientUser.getUserName();
                Assert.assertNotNull(name);
                KosmoSUser user2 = (KosmoSUser) CommonBase.controller.getUser(name);
                Assert.assertNotNull(user2);
                name = CommonBase.clientUser2.getUserName();
                Assert.assertNotNull(name);
                KosmoSUser user3 = (KosmoSUser) CommonBase.controller.getUser(name);
                Assert.assertNotNull(user3);
                Group group = null;
                try {
                    group = CommonBase.controller.getGroup(TestGroup.testGroupDevicesGroupName, CacheMode.CACHE_AND_PERSISTENCE);
                    Assert.assertNotNull(group);

                } catch (GroupNotFoundException e) {
                    Assert.fail("could not find group again!");
                }
                Assert.assertTrue(group.hasUser(user3));
                Device device = null;
                try {
                    device = CommonBase.controller.getDevice(TestGroup.testGroupDevicesDeviceName);
                } catch (DeviceNotFoundException ex) {
                    Assert.fail("could not find Device " + TestGroup.testGroupDevicesDeviceName + " again");
                }
                Assert.assertNotNull(device);
                try {
                    Assert.assertTrue(device.canRead(user3));
                } catch (NoAccessToScope noAccessToScope) {
                    Assert.fail(noAccessToScope.getMessage());
                }
                try {
                    Device.Location loc = controller.getLocation(user2, "FakeMultiSensor127");
                    Assert.assertNotNull(loc, "could not get get location after restart");
                    Utils.compare(loc.toJSON(), jsonCache.get("location_FakeMultiSensor127"));
                } catch (DeviceNotFoundException | NoAccessToScope e) {
                    Assert.fail(e.getMessage());
                }
                Device d = null;
                try {
                    d = controller.getDevice("FakeMultiSensor124");

                } catch (DeviceNotFoundException exception) {
                    Assert.fail("could not find FakeMultiSensor124 again after restart");
                }
/*                Utils.compare(jsonCache.get("state_FakeMultiSensor124"), d);*/
                String integrationTests = System.getProperty("integrationTests");
                logger.info("integrationTests {}", integrationTests);

                if ("1".equals(integrationTests)) {
                    logger.info("retesting HA");
                    HAClientTest.reTestDevices();
                }
                try {
                    d = controller.getDevice(TestDevice.texts_device_name);
                    Assert.assertEquals(d.getText("description").getValue(),"test2");
                } catch (DeviceNotFoundException exception) {
                    Assert.fail(String.format("could not find %s again after restart",TestDevice.texts_device_name));
                }
                TestDevice.retest();

                //System.out.println("Profile ID:  " + System.getProperty("profileId"));
            }



            controller.stop();
        }

    }


}
