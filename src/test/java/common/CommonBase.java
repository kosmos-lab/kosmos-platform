package common;

import CI.HTTP.TestDevice;
import CI.HTTP.TestGroup;
import Integration.HAClientTest;
import de.kosmos_lab.platform.KosmoSController;
import de.kosmos_lab.platform.client.KosmoSHTTPClient;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.KosmoSUser;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.GroupNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.platform.persistence.Constants.RunMode;
import de.kosmos_lab.platform.utils.KosmoSHelper;
import de.kosmos_lab.utils.FileUtils;
import de.kosmos_lab.utils.JSONChecker;
import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.utils.StringFunctions;
import de.kosmos_lab.utils.exceptions.CompareException;
import de.kosmos_lab.web.server.WebServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@SuppressFBWarnings("MS_CANNOT_BE_FINAL")
public class CommonBase {
    final public static ConcurrentHashMap<String, JSONObject> jsonCache = new ConcurrentHashMap<>();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("Test");
    public static String pass;

    public static KosmoSController controller;
    public static KosmoSHTTPClient httpClientAdmin;
    public static KosmoSHTTPClient httpClientUser;
    public static KosmoSHTTPClient httpClientUser2;
    public static KosmoSHTTPClient httpClientUser3;

    public static KosmoSHTTPClient httpClientFakeUser;
    public static String baseUrl = "";
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static KosmoSHTTPClient clientha;
    //public static KosmoSClient clientAdmin;
    //public static KosmoSClient clientUser;
    //private static KosmoSClient clientUser2;


    @SuppressFBWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    @BeforeSuite
    public static void prepare() {
        String TEST_HOST = KosmoSHelper.getEnv("TEST_HOST");
        logger.info("Test host:{}", TEST_HOST);
        if (TEST_HOST == null || TEST_HOST.length() == 0) {

            File testdb = KosmoSController.getFile("db/db.sqlite", RunMode.TEST);
            if (testdb.exists()) {
                //Assert.assertTrue(testdb.delete(),"could not delete old test db!!");
                logger.info("deleting old DB {}", testdb);
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
                        logger.info("deleting old file {}", f);
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
                if (!testConf.delete()) {
                    Assert.fail(String.format("could not delete old test config %s", testConf));

                }
            }
            setup();

            try {
                //clientAdmin =
                httpClientAdmin = new KosmoSHTTPClient(baseUrl, "admin", StringFunctions.generateRandomKey());
                //clientUser = new KosmoSClient(baseUrl, "user", StringFunctions.generateRandomKey());
                httpClientUser = new KosmoSHTTPClient(baseUrl, "user", StringFunctions.generateRandomKey());
                //clientUser2 = new KosmoSClient(baseUrl, "user2", StringFunctions.generateRandomKey());
                httpClientUser2 = new KosmoSHTTPClient(baseUrl, "user2", StringFunctions.generateRandomKey());
                httpClientUser3 = new KosmoSHTTPClient(baseUrl, "user3", StringFunctions.generateRandomKey());

                httpClientFakeUser = new KosmoSHTTPClient(baseUrl, "fakeuser2", "test");
                FileUtils.writeToFile(KosmoSController.getFile("users.json", RunMode.TEST), new JSONObject().put("admin", httpClientAdmin.getPassword()).put("user", httpClientUser.getPassword()).put("user2", httpClientUser2.getPassword()).put("user3", httpClientUser3.getPassword()).toString());

            } catch (Exception e) {
                logger.error("could not create users and write the information to a seperate file!", e);
            }
            String pass = httpClientAdmin.getPassword();
            Assert.assertNotNull(pass);
            controller.addUser("admin", pass, 1000);
            controller.addUser("ha", "pass", 1);
        } else {
            logger.info("using services for kosmos");
            baseUrl = "http://" + TEST_HOST;
            try {
                httpClientFakeUser = new KosmoSHTTPClient(baseUrl, "fakeuser2", "test");
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
                                //clientAdmin = new KosmoSClient(baseUrl, username, obj.getString("password"));
                                httpClientAdmin = new KosmoSHTTPClient(baseUrl, username, obj.getString("password"));
                            } catch (Exception e) {
                                logger.error("could not login as admin!", e);
                            }
                        }
                        if (username.equals("user")) {
                            try {
                                //clientUser = new KosmoSClient(baseUrl, username, obj.getString("password"));
                                httpClientUser = new KosmoSHTTPClient(baseUrl, username, obj.getString("password"));
                                //httpClientUser = new KosmoSHTTPClient(baseUrl, username, obj.getString("password"));
                            } catch (Exception e) {
                                logger.error("could not login as user!", e);
                                //
                            }
                        }
                        if (username.equals("user2")) {

                            try {
                                //clientUser2 = new KosmoSClient(baseUrl, username, obj.getString("password"));
                                httpClientUser2 = new KosmoSHTTPClient(baseUrl, username, obj.getString("password"));
                                //httpClientUser2 = new KosmoSHTTPClient(baseUrl, username, obj.getString("password"));
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

    public static void setup() {
        JSONObject config = new JSONObject();

        File testConf = KosmoSController.getFile("config.json", RunMode.TEST);
        if (testConf.exists()) {
            try {
                config = new JSONObject(KosmosFileUtils.readFile(testConf));
            } catch (Exception ex) {
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
                logger.warn("Exception while comparing {} to {}: {}", key, expected, e.getMessage(), e);
            }
            //logger.info(device.toString());
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

    public static boolean waitForValueMapped(Map<String, Map<String, Object>> fullMap, String key1, String key, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        CompareException laste = null;
        while (true) {
            try {
                Map<String, Object> map = fullMap.get(key1);
                if (map != null) {
                    if (JSONChecker.equals(map.get(key), expected)) {
                        return true;
                    }
                }
            } catch (CompareException e) {
                //dont spam the log for now
                laste = e;
            } catch (Exception e) {
                //e.printStackTrace();
                logger.warn("Exception while comparing {} to {}: {}", key, expected, e.getMessage(), e);
            }

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

    public static boolean waitForValue(Map<String, Object> map, String key, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        CompareException laste = null;
        while (true) {
            try {
                if (JSONChecker.equals(map.get(key), expected)) {
                    return true;
                }
            } catch (CompareException e) {
                //dont spam the log for now
                laste = e;
            } catch (Exception e) {
                //e.printStackTrace();
                logger.warn("Exception while comparing {} to {}: {}", key, expected, e.getMessage(), e);
            }

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

    public static boolean waitForValue(AtomicReference ref, Object expected, long waittime) {
        long started = System.currentTimeMillis();
        CompareException laste = null;
        while (true) {
            try {
                if (JSONChecker.equals(ref.get(), expected)) {
                    return true;
                }
            } catch (CompareException e) {
                //dont spam the log for now
                laste = e;
            } catch (Exception e) {
                //e.printStackTrace();
                logger.warn("Exception while comparing {} to {}: {}", ref, expected, e.getMessage(), e);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("could not sleep!", e);
            }
            long delta = System.currentTimeMillis() - started;
            if (delta > waittime) {
                if (laste != null) {
                    logger.warn("could not match {} to {}: {}", ref, expected, laste.getMessage());
                }
                return false;
            }
        }
    }

    public static boolean waitForValueJSONHttp(KosmoSHTTPClient client, String url, String key, Object expected, long waittime) {
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
                    System.out.printf("Test FINISHED! you can now login via %s %s and stop the server once you are done to exit the test%n", httpClientAdmin.getUserName(), httpClientAdmin.getPassword());
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

                } else {
                    CommonBase.restart();
                }
                Map<String, ITestNGMethod> methods = new HashMap<>();

                for (ITestNGMethod method : context.getPassedTests().getAllMethods()) {

                    String n = String.format("%s.%s", method.getConstructorOrMethod().getDeclaringClass().getCanonicalName(), method.getMethodName());
                    logger.info("found passed test {}", n);
                    methods.put(n, method);
                }

                if (methods.containsKey("common.CommonBase.createUser")) {
                    logger.info("Checking if users still exist");

                    String name = CommonBase.httpClientAdmin.getUserName();
                    Assert.assertNotNull(name);

                    KosmoSUser admin = (KosmoSUser) CommonBase.controller.getUser(name);
                    Assert.assertNotNull(admin);
                    Assert.assertTrue(admin.isAdmin());
                    name = CommonBase.httpClientUser.getUserName();
                    Assert.assertNotNull(name);
                    KosmoSUser user2 = (KosmoSUser) CommonBase.controller.getUser(name);
                    Assert.assertNotNull(user2);
                    name = CommonBase.httpClientUser2.getUserName();
                    Assert.assertNotNull(name);
                    KosmoSUser user3 = (KosmoSUser) CommonBase.controller.getUser(name);
                    Assert.assertNotNull(user3);
                    if (methods.containsKey("CI.HTTP.TestGroup.testGroups")) {
                        logger.info("Checking if groups still exist");
                        Group group = null;
                        try {
                            group = CommonBase.controller.getGroup(TestGroup.testGroupDevicesGroupName, CacheMode.CACHE_AND_PERSISTENCE);
                            Assert.assertNotNull(group);

                        } catch (GroupNotFoundException e) {
                            Assert.fail("could not find group again!");
                        }
                        Assert.assertTrue(group.hasUser(user3));
                    }
                    if (methods.containsKey("CI.HTTP.TestDevice.deviceTest")) {
                        logger.info("Checking if devices still exist");

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
                            Assert.assertEquals(d.getText("description").getValue(), "test2");
                        } catch (DeviceNotFoundException exception) {
                            Assert.fail(String.format("could not find %s again after restart", TestDevice.texts_device_name));
                        }
                        TestDevice.retest();

                    }


                }


                //System.out.println("Profile ID:  " + System.getProperty("profileId"));
            }


            controller.stop();
        }

    }

    @Test(priority = 10, groups = {"login"})
    public void login() {
        Assert.assertNotNull(CommonBase.httpClientAdmin);
        Assert.assertNotNull(CommonBase.httpClientFakeUser);
        Assert.assertNotNull(CommonBase.httpClientUser);
        Assert.assertNotNull(CommonBase.httpClientUser2);
        InputStream is = null;
        BufferedReader br = null;
        while (true) {

            try {
                String h = baseUrl + "/server/alive";
                logger.info("checking {}", h);
                URL url = new URL(h);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String content = br.readLine();
                br.close();

                is.close();
                if (content != null) {
                    if (content.equals("online")) {
                        break;
                    }
                }
                logger.info("content did not match! {}", content);


            } catch (IOException ex) {
                logger.warn("Exception while checking status of KosmoS Server: {}", ex.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
        Assert.assertTrue(CommonBase.httpClientAdmin.refreshToken(), "Login failed!");
        //logger.info("testing admin login");


        //logger.info("testing user2 login");
        Assert.assertFalse(CommonBase.httpClientFakeUser.refreshToken(), "Login should have failed!");
        //Assert.assertTrue(CommonBase.clientFakeUser.refreshToken(), "Login should have failed!");
        ContentResponse response = httpClientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", httpClientAdmin.getUserName()).put("pass", httpClientAdmin.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "raw Login failed");


        response = httpClientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", httpClientAdmin.getUserName()).put("pass", StringFunctions.generateRandomKey()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "raw Login should have failed");
        response = httpClientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", StringFunctions.generateRandomKey()).put("pass", StringFunctions.generateRandomKey()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "raw Login should have failed");
        response = httpClientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_MISSING_VALUE, "raw Login should have failed");
    }

    @Test(dependsOnGroups = {"login"}, groups = {"createUser"})
    public void createUser() {
        ContentResponse response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser.getUserName()).put("pass", httpClientUser.getPassword()));
        Assert.assertNotNull(response);
        logger.info("sent user add request");

        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("added new user");
        Assert.assertTrue(CommonBase.httpClientUser.refreshToken(), "Login with new user failed!");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser.getUserName()).put("pass", httpClientUser.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 409, "Duplicated user did not fail!");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser2.getUserName()).put("pass", httpClientUser2.getPassword()).put("level", 1000));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Should not be able to create a user with more access than myself!");
        response = CommonBase.httpClientUser.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser2.getUserName()).put("pass", httpClientUser2.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "User add should not be possible without admin privilege!");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser2.getUserName()).put("pass", httpClientUser2.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.httpClientUser3.getUserName()).put("pass", httpClientUser3.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");

        Assert.assertTrue(CommonBase.httpClientUser2.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(CommonBase.httpClientUser3.refreshToken(), "Login with new user failed!");


    }
}
