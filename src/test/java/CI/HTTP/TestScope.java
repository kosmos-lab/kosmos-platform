package CI.HTTP;

import common.CommonBase;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestScope {
    
    
    @Test(groups = "testScopeDevices",dependsOnGroups = {"testGroups","deviceTest"})
    public void testScopeDevices() {
        String scopeName = "testscope1";
        String deviceName = "multiscope1";
        
        ContentResponse response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("scopes", new JSONObject().put("read", scopeName + ":read").put("del", scopeName + ":del").put("write", scopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
        
        response = CommonBase.clientUser.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device was not readable");
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device was readable");
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
        response = CommonBase.clientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":read").put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope add user did fail!");
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device was not readable");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
        response = CommonBase.clientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope add user did fail!");
        
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device delete did NOT fail!");
        response = CommonBase.clientUser.getResponse("/scope/adduser", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":del").put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope add user did fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Device delete did fail!");
        
        
        
        
        
    }
    @Test(groups = "testScopeDevices",dependsOnGroups = {"testGroups","deviceTest"})
    public void testScopeDevicesUser() {
        String scopeName = "testscope2";
        String deviceName = "multiscope2";
        String scopeGroup = "scopeGroup1";
        //create device with scope
        ContentResponse response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", deviceName).put("scopes", new JSONObject().put("read", scopeName + ":read").put("del", scopeName + ":del").put("write", scopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
        
        
        //check if device is readable for owner
        response = CommonBase.clientUser.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device was not readable");
        
        
        //check if device is NOT readable by different user
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + deviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device was readable for user that should not be able to read it");
        
        //check if owner can set
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        
        //check if another user can NOT set
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
        
        //check if another user can NOT delete
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", deviceName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device delete did NOT fail!");
        
        //add a non existing group to the scope
        response = CommonBase.clientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("group", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 404, "scope add group did NOT fail!");
    
        response = CommonBase.clientUser.getResponse("/group/add", HttpMethod.POST, new JSONObject().put("name", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "group add returned wrong status!");
    
    
        response = CommonBase.clientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", scopeName + ":write").put("group", scopeGroup));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope add group did fail!");
        
        
        //user was able to set - this one is technically useless but still
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 20));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
    
    
        //add user to group
        response = CommonBase.clientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "group add user did fail!");
        
        //check if the user can read the state now
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
        
        //scope delete user from group
        response = CommonBase.clientUser.getResponse("/group/deluser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope del user did fail!");
        
        //user should not be able to set now
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 19));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 403, "Device set did NOT fail!");
    
        
        //add user as admin
        response = CommonBase.clientUser.getResponse("/group/addadmin", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope add user did fail!");
        
        //user should be able to set again
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", deviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "Device set did fail!");
    
        //scope delete user from group
        response = CommonBase.clientUser.getResponse("/group/deluser", HttpMethod.POST, new JSONObject().put("group", scopeGroup).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 204, "scope del user did fail!");
        
        
        
        
        
    }
}
