package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.web.server.WebServer;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestScope {


    @Test(groups = "testScopeDevices", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testScopeDevices() {
        String scopeName = "testscope1";
        String deviceName = "multiscope1";

        ContentResponse response = CommonBase.httpClientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("scopes", new JSONObject().put("read", scopeName + ":read").put("del", scopeName + ":del").put("write", scopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");

        response = CommonBase.httpClientUser.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was not readable");
        response = CommonBase.httpClientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device was readable");
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        response = CommonBase.httpClientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        response = CommonBase.httpClientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":read").put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");
        response = CommonBase.httpClientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was not readable");
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        response = CommonBase.httpClientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");

        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.httpClientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device delete did NOT fail!");
        response = CommonBase.httpClientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":del").put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");
        response = CommonBase.httpClientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Device delete did fail!");


    }

    @Test(groups = "testScopeDevices", dependsOnGroups = {"testGroups", "deviceTest"})
    public void testScopeDevicesUser() {
        String scopeName = "testscope2";
        String deviceName = "multiscope2";
        String scopeGroup = "scopeGroup1";
        //create device with scope
        ContentResponse response = CommonBase.httpClientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("scopes", new JSONObject().put("read", scopeName + ":read").put("del", scopeName + ":del").put("write", scopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");


        //check if device is readable for owner
        response = CommonBase.httpClientUser.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was not readable");


        //check if device is NOT readable by different user
        response = CommonBase.httpClientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device was readable for user that should not be able to read it");

        //check if owner can set
        response = CommonBase.httpClientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");

        //check if another user can NOT set
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");

        //check if another user can NOT delete
        response = CommonBase.httpClientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device delete did NOT fail!");

        //add a non existing group to the scope
        response = CommonBase.httpClientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("group", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NOT_FOUND, "scope add group did NOT fail!");

        response = CommonBase.httpClientUser.getResponse("/group/add", HttpMethod.POST, new JSONObject().put("name", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "group add returned wrong status!");


        response = CommonBase.httpClientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("group", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add group did fail!");


        //user was able to set - this one is technically useless but still
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 20));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");


        //add user to group
        response = CommonBase.httpClientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "group add user did fail!");

        //check if the user can read the state now
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");

        //scope delete user from group
        response = CommonBase.httpClientUser.getResponse("/group/deluser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope del user did fail!");

        //user should not be able to set now
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");


        //add user as admin
        response = CommonBase.httpClientUser.getResponse("/group/addadmin", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");

        //user should be able to set again
        response = CommonBase.httpClientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");

        //scope delete user from group
        response = CommonBase.httpClientUser.getResponse("/group/deluser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.httpClientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope del user did fail!");


    }
}
