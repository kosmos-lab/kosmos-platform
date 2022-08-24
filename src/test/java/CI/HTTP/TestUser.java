package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.platform.client.KosmoSClient;
import de.kosmos_lab.utils.StringFunctions;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static common.CommonBase.*;

public class TestUser {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("TestUser");


    @Test(priority = 10, groups = {"login"})
    public void login() {
        Assert.assertNotNull(CommonBase.clientAdmin);
        Assert.assertNotNull(CommonBase.clientFakeUser);
        Assert.assertNotNull(CommonBase.clientUser);
        Assert.assertNotNull(CommonBase.clientUser2);
        InputStream is = null;
        BufferedReader br = null;
        while (true) {

            try {
                String h = baseUrl + "/server/alive";
                logger.info("checking {}",h);
                URL url = new URL(h);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8 ));
                String content = br.readLine();
                br.close();

                is.close();
                if( content != null) {
                    if (content.equals("online")) {
                        break;
                    }
                }
                logger.info("content did not match! {}", content);


            } catch (IOException  ex) {
                logger.warn("Exception while checking status of KosmoS Server: {}", ex.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            finally {
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
        Assert.assertTrue(CommonBase.clientAdmin.refreshToken(), "Login failed!");
        //logger.info("testing admin login");


        //logger.info("testing user2 login");
        Assert.assertFalse(CommonBase.clientFakeUser.refreshToken(), "Login should have failed!");
        //Assert.assertTrue(CommonBase.clientFakeUser.refreshToken(), "Login should have failed!");
        ContentResponse response = clientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", clientAdmin.getUserName()).put("pass", clientAdmin.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "raw Login failed");


        response = clientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", clientAdmin.getUserName()).put("pass", StringFunctions.generateRandomKey()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "raw Login should have failed");
        response = clientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject().put("user", StringFunctions.generateRandomKey()).put("pass", StringFunctions.generateRandomKey()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "raw Login should have failed");
        response = clientAdmin.getResponse("/user/login", HttpMethod.POST, new JSONObject());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_MISSING_VALUE, "raw Login should have failed");
    }

    @Test(dependsOnGroups = {"login"}, groups = {"createUser"})
    public void createUser() {
        ContentResponse response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser.getUserName()).put("pass", clientUser.getPassword()));
        Assert.assertNotNull(response);
        logger.info("sent user add request");
    
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("added new user");
        Assert.assertTrue(CommonBase.clientUser.refreshToken(), "Login with new user failed!");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser.getUserName()).put("pass", clientUser.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 409, "Duplicated user did not fail!");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser2.getUserName()).put("pass", clientUser2.getPassword()).put("level", 1000));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Should not be able to create a user with more access than myself!");
        response = CommonBase.clientUser.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser2.getUserName()).put("pass", clientUser2.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "User add should not be possible without admin privilege!");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser2.getUserName()).put("pass", clientUser2.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", CommonBase.clientUser3.getUserName()).put("pass", clientUser3.getPassword()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
    
        Assert.assertTrue(CommonBase.clientUser2.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(CommonBase.clientUser3.refreshToken(), "Login with new user failed!");


    }

    @Test(groups = {"setPassword"}, dependsOnGroups = {"createUser"})
    public void setPassword() {
        String pass = StringFunctions.generateRandomKey();

        try {
            KosmoSClient clientUser4 = new KosmoSClient(baseUrl, "user4", pass);
            ContentResponse response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
            Assert.assertTrue(clientUser4.refreshToken(), "Login with new user failed!");
            String pass2 = StringFunctions.generateRandomKey();
            while (pass.equalsIgnoreCase(pass2)) {
                pass2 = StringFunctions.generateRandomKey();
            }
            response = CommonBase.clientAdmin.getResponse("/user/password", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass2));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "password not set!");
            Assert.assertFalse(clientUser4.refreshToken(), "Login with old password was possible!");
            clientUser4.setPassword(pass2);
            Assert.assertTrue(clientUser4.refreshToken(), "Login with new password was not possible!");
            String pass3 = StringFunctions.generateRandomKey();
            while (pass3.equalsIgnoreCase(pass2)) {
                pass3 = StringFunctions.generateRandomKey();
            }
            response = clientUser4.getResponse("/user/password", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass3));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "password set was not possible on self");
            clientUser4.setPassword(pass3);

            Assert.assertTrue(clientUser4.refreshToken(), "Login with new password was not possible!");
            String pass4 = StringFunctions.generateRandomKey();
            while (pass4.equalsIgnoreCase(pass3)) {
                pass4 = StringFunctions.generateRandomKey();
            }
            response = CommonBase.clientUser.getResponse("/user/password", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass4));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 403, "password set possible on other user without admin");
            Assert.assertTrue(clientUser4.refreshToken(), "Login with old password was not possible!");
            clientUser4.setPassword(pass4);
            Assert.assertFalse(clientUser4.refreshToken(), "Login with new password was possible!");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"deleteUser"}, dependsOnGroups = {"createUser"})
    public void deleteUser() throws Exception {
        String pass = StringFunctions.generateRandomKey();
        KosmoSClient clientUser10 = new KosmoSClient(baseUrl, "user10", pass);
        KosmoSClient clientUser11 = new KosmoSClient(baseUrl, "user11", pass);
        KosmoSClient clientAdmin2 = new KosmoSClient(baseUrl, "admin10", pass);
        logger.info("adding user user10");
        ContentResponse response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser10.getUserName()).put("pass", pass));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("adding user user11");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser11.getUserName()).put("pass", pass));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("adding user admin10");
        response = CommonBase.clientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientAdmin2.getUserName()).put("pass", pass).put("level", 100));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");

        Assert.assertTrue(clientUser10.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(clientUser11.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(clientAdmin2.refreshToken(), "Login with new user failed!");

        response = clientUser10.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser11.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Could delete user without admin level!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", CommonBase.clientAdmin.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Could delete user with more access!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser11.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser10.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        response = CommonBase.clientAdmin.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientAdmin2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        Assert.assertFalse(clientUser10.refreshToken(), "Login with deleted user NOT failed!");
        Assert.assertFalse(clientUser11.refreshToken(), "Login with deleted user NOT failed!");
        Assert.assertFalse(clientAdmin2.refreshToken(), "Login with deleted user NOT failed!");

    }
}
