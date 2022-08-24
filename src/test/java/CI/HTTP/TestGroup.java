package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.platform.client.KosmoSClient;
import de.kosmos_lab.platform.data.Group;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestGroup {
    
    public final static String testGroupsGroupName = "testgroup1";
    public final static String testGroupDevicesScopeName = "testscope3";
    public final static String testGroupDevicesDeviceName = "multiscope3";
    public final static String testGroupDevicesGroupName = "testgroup3";
    
    private void assertAdminInGroup(KosmoSClient client, String group) {
        Assert.assertTrue(checkMyAccessTo(client, group,MyListType.Group,MyAccessType.Admin), String.format("User %s seems to NOT be admin in group %s", client.getUserName(), group));
    }
    
    private void assertNotAdminInGroup(KosmoSClient client, String group) {
        Assert.assertFalse(checkMyAccessTo(client, group,MyListType.Group,MyAccessType.Admin), String.format("User %s seems to be admin in group %s", client.getUserName(), group));
    }
    
    private void assertNotUserInGroup(KosmoSClient client, String group) {
        Assert.assertFalse(checkMyAccessTo(client, group,MyListType.Group,MyAccessType.User), String.format("User %s seems to be user in group %s", client.getUserName(), group));
    }
    
    private void assertUserInGroup(KosmoSClient client, String group) {
        Assert.assertTrue(checkMyAccessTo(client, group,MyListType.Group,MyAccessType.User), String.format("User %s seems to NOT be user in group %s", client.getUserName(), group));
    }

    private boolean checkMyAccessTo(KosmoSClient client, String name, MyListType type, MyAccessType access) {
        ContentResponse response;
        switch (type) {
            case Group:
                response = client.getResponse("/group/my", HttpMethod.GET);
                break;
            case Scope:
                response = client.getResponse("/scope/my", HttpMethod.GET);
                break;
            default:
                throw new IllegalArgumentException(String.format("Could not get information about %s", type));
        }
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "list my "+type+" failed!");
        
        JSONObject obj = new JSONObject(response.getContentAsString());
        JSONArray arr;
        switch (access) {
            case User:
                arr = obj.getJSONArray("user");
                break;
            case Admin:
                arr = obj.getJSONArray("admin");
                break;
            default:
                throw new IllegalArgumentException(String.format("Could not understand access type %s", access));
        }
        for (int i = 0; i < arr.length(); i++) {
            String n = arr.getString(i);
            if (n.equalsIgnoreCase(name)) {
                return true;
                
            }
            
        }
        return false;
    }
    
    @Test(groups = {"testGroupDevices"}, dependsOnGroups = {"testScopeDevices", "testGroups"})
    public void testGroupDevices() {
        
        
        ContentResponse response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", testGroupDevicesDeviceName).put("scopes", new JSONObject().put("read", testGroupDevicesScopeName + ":read").put("del", testGroupDevicesScopeName + ":del").put("write", testGroupDevicesScopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/FakeMultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
        
        response = CommonBase.clientUser.getResponse("/device/get?id=" + testGroupDevicesDeviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was not readable");
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + testGroupDevicesDeviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device was readable");
        response = CommonBase.clientUser.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 17));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        
        
        response = CommonBase.clientUser.getResponse("/group/add", HttpMethod.POST, new JSONObject().put("name", testGroupDevicesGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "addgroupuser did fail!");
        response = CommonBase.clientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupDevicesGroupName).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "addgroupuser did fail!");
        response = CommonBase.clientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", testGroupDevicesScopeName + ":read").put("group", testGroupDevicesGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "addgroupuser did fail!");
        response = CommonBase.clientUser2.getResponse("/device/get?id=" + testGroupDevicesDeviceName, HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device was not readable");
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device set did NOT fail!");
        response = CommonBase.clientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", testGroupDevicesScopeName + ":write").put("group", testGroupDevicesGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");
        
        response = CommonBase.clientUser2.getResponse("/device/set", HttpMethod.POST, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Device set did fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "Device delete did NOT fail!");
        response = CommonBase.clientUser.getResponse("/scope/addgroup", HttpMethod.POST, new JSONObject().put("scope", testGroupDevicesScopeName + ":del").put("group", testGroupDevicesGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "scope add user did fail!");
        response = CommonBase.clientUser2.getResponse("/device/delete", HttpMethod.DELETE, new JSONObject().put("id", testGroupDevicesDeviceName).put("currentEnvironmentTemperature", 18));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device delete did fail!");
        response = CommonBase.clientUser.getResponse("/device/add", HttpMethod.POST, new JSONObject().put("uuid", testGroupDevicesDeviceName).put("scopes", new JSONObject().put("read", testGroupDevicesScopeName + ":read").put("del", testGroupDevicesScopeName + ":del").put("write", testGroupDevicesScopeName + ":write")).put("schema", "https://kosmos-lab.de/schema/MultiSensor.json"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "Device add did fail!");
    }
    
    @Test(groups = {"testGroups"}, dependsOnGroups = {"createUser"})
    public void testGroups() {
        Group g;
        try {
            g = CommonBase.controller.getPersistence().getGroup(testGroupsGroupName);
            Assert.fail("group should not exist!");
        } catch (NotFoundInPersistenceException e) {
            //e.printStackTrace();
        }
        
        assertNotAdminInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotAdminInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertNotAdminInGroup(CommonBase.clientUser3, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser3, testGroupsGroupName);
        ContentResponse response = CommonBase.clientUser.getResponse("/group/add", HttpMethod.POST, new JSONObject().put("name", testGroupsGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "group add returned wrong status!");
        assertAdminInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotAdminInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertNotAdminInGroup(CommonBase.clientUser3, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser3, testGroupsGroupName);
        try {
            g = CommonBase.controller.getPersistence().getGroup(testGroupsGroupName);
        } catch (NotFoundInPersistenceException e) {
            //e.printStackTrace();
            Assert.fail("group should exist!");
    
        }
        response = CommonBase.clientUser2.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "was able to hijack user adding");
        assertNotUserInGroup(CommonBase.clientUser2, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser3.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "was able to hijack user adding");
        assertNotUserInGroup(CommonBase.clientUser3, testGroupsGroupName);
        
        response = CommonBase.clientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", "afaksjnfkajsnfas").put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NOT_FOUND, "group that should not exist was found");
        
        response = CommonBase.clientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", "23592835235asdf"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NOT_FOUND, "user that should not exist was found");
        
        response = CommonBase.clientUser.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not add user to group?");
        assertNotAdminInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertUserInGroup(CommonBase.clientUser2, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser3.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "was able to hijack user adding");
        assertNotUserInGroup(CommonBase.clientUser3, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/addadmin", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "was able to hijack user adding");
        assertNotAdminInGroup(CommonBase.clientUser2, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/addadmin", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser3.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_FORBIDDEN, "was able to hijack user adding");
        assertNotAdminInGroup(CommonBase.clientUser3, testGroupsGroupName);
        
        
        response = CommonBase.clientUser.getResponse("/group/addadmin", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser2.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not add user to group?");
        assertAdminInGroup(CommonBase.clientUser2, testGroupsGroupName);
        assertUserInGroup(CommonBase.clientUser2, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/deladmin", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not del admin from group?");
        assertNotAdminInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser, testGroupsGroupName);
        
        
        response = CommonBase.clientUser2.getResponse("/group/adduser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not add user to group?");
        assertNotAdminInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertUserInGroup(CommonBase.clientUser, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/deluser", HttpMethod.POST, new JSONObject().put("group", testGroupsGroupName).put("user", CommonBase.clientUser.getUserName()));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not add user to group?");
        assertNotAdminInGroup(CommonBase.clientUser, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientUser, testGroupsGroupName);
        
        response = CommonBase.clientUser2.getResponse("/group/delete", HttpMethod.POST,new JSONObject().put("group", testGroupsGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "could not delete group?");
        response = CommonBase.clientUser3.getResponse("/group/get", HttpMethod.GET,new JSONObject().put("name", testGroupsGroupName));
        Assert.assertNotNull(response);

        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NOT_FOUND, "could find deleted group again");
    
        try {
            g = CommonBase.controller.getPersistence().getGroup(testGroupsGroupName);
            Assert.fail("group should not exist!");
        } catch (NotFoundInPersistenceException e) {
            //e.printStackTrace();
        }
        assertNotAdminInGroup(CommonBase.clientAdmin, testGroupsGroupName);
        assertNotUserInGroup(CommonBase.clientAdmin, testGroupsGroupName);
        response = CommonBase.clientUser.getResponse("/group/add", HttpMethod.POST, new JSONObject().put("name", testGroupsGroupName));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE, "group add returned wrong status!");
        try {
            g = CommonBase.controller.getPersistence().getGroup(testGroupsGroupName);
            
        } catch (NotFoundInPersistenceException e) {
            Assert.fail("group should exist!");
        }
    }
    
    private enum MyListType {Group, Scope}
    
    private enum MyAccessType {User, Admin}
    
}
