package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.platform.client.KosmoSHTTPClient;
import de.kosmos_lab.utils.StringFunctions;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static common.CommonBase.baseUrl;

public class TestUser {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("TestUser");


    @Test(groups = {"setPassword"}, dependsOnGroups = {"createUser"})
    public void setPassword() {
        String pass = StringFunctions.generateRandomKey();

        try {
            KosmoSHTTPClient clientUser4 = new KosmoSHTTPClient(baseUrl, "user4", pass);
            ContentResponse response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
            Assert.assertTrue(clientUser4.refreshToken(), "Login with new user failed!");
            String pass2 = StringFunctions.generateRandomKey();
            while (pass.equalsIgnoreCase(pass2)) {
                pass2 = StringFunctions.generateRandomKey();
            }
            response = CommonBase.httpClientAdmin.getResponse("/user/password", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass2));
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
            response = CommonBase.httpClientUser.getResponse("/user/password", HttpMethod.POST, new JSONObject().put("user", clientUser4.getUserName()).put("pass", pass4));
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
        KosmoSHTTPClient clientUser10 = new KosmoSHTTPClient(baseUrl, "user10", pass);
        KosmoSHTTPClient clientUser11 = new KosmoSHTTPClient(baseUrl, "user11", pass);
        KosmoSHTTPClient clientAdmin2 = new KosmoSHTTPClient(baseUrl, "admin10", pass);
        logger.info("adding user user10");
        ContentResponse response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser10.getUserName()).put("pass", pass));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("adding user user11");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientUser11.getUserName()).put("pass", pass));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");
        logger.info("adding user admin10");
        response = CommonBase.httpClientAdmin.getResponse("/user/add", HttpMethod.POST, new JSONObject().put("user", clientAdmin2.getUserName()).put("pass", pass).put("level", 100));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "User add returned wrong status!");

        Assert.assertTrue(clientUser10.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(clientUser11.refreshToken(), "Login with new user failed!");
        Assert.assertTrue(clientAdmin2.refreshToken(), "Login with new user failed!");

        response = clientUser10.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser11.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Could delete user without admin level!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", CommonBase.httpClientAdmin.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Could delete user with more access!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser11.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        response = clientAdmin2.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientUser10.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        response = CommonBase.httpClientAdmin.getResponse("/user/delete", HttpMethod.DELETE, new JSONObject().put("user", clientAdmin2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Could NOT delete user!");
        Assert.assertFalse(clientUser10.refreshToken(), "Login with deleted user NOT failed!");
        Assert.assertFalse(clientUser11.refreshToken(), "Login with deleted user NOT failed!");
        Assert.assertFalse(clientAdmin2.refreshToken(), "Login with deleted user NOT failed!");

    }
}
