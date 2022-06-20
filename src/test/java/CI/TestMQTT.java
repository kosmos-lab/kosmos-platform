package CI;


import common.CommonBase;
import de.kosmos_lab.kosmos.data.Device;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class TestMQTT implements IMqttMessageListener{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("TestMQTT");
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("MQTT {}:{}",topic,new String(message.getPayload(), StandardCharsets.UTF_8));
    }
    
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @Test(dependsOnGroups = "addSchema",groups = "MQTT")
    public void createDeviceMQTT() {
        boolean succ = false;
        try {
            MqttClient client = new MqttClient("tcp://localhost:1884", "createDeviceMQTT", new MemoryPersistence());
        
            MqttConnectOptions connOpts = new MqttConnectOptions();
            String pass = CommonBase.clientAdmin.getPassword();
            Assert.assertNotNull(pass);
            connOpts.setPassword(pass.toCharArray());
            connOpts.setUserName(CommonBase.clientAdmin.getUserName());
            client.connect(connOpts);
            logger.info("MQTT Connected");
            client.subscribeWithResponse("#", this);
            logger.info("MQTT Subbed");
            
            client.setTimeToWait(5000);
            client.publish("kosmos/multi3/config",new JSONObject().put("schema","https://kosmos-lab.de/schema/MultiSensor.json").toString().getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT Published");
            Thread.sleep(500);
            Device d = CommonBase.controller.getDevice("multi3");
            Assert.assertNotNull(d,"could not find device!");

            
            client.publish("kosmos/multi3/set",new JSONObject().put("currentEnvironmentTemperature",19).toString().getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT pub2");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",19,10000l),"MQTT set failed");
            client.publish("kosmos/multi3/set",new JSONObject().put("currentEnvironmentTemperature",13).toString().getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT pub3");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",13,10000l),"MQTT set failed");
    
            client.publish("kosmos/multi3/currentEnvironmentTemperature/set","15".getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT pub4 ");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",15,10000l),"MQTT set by key failed");
            
            succ = true;
            client.disconnect();
            
        }
        catch (Exception ex) {
            if ( !succ) {
                Assert.fail("MQTT Connection error",ex);
            }
            //Assert.fail("failed to connect",ex);
            ex.printStackTrace();
        }
        
    
    }
    @Test(groups = {"testMQTTPublishWithoutLogin"},dependsOnGroups = {"addSchema"})
    public void testMQTTPublishWithoutLogin() {
        try {
            logger.info("trying to force a connection without login");
            MqttClient client = new MqttClient("tcp://localhost:1884", "testMQTTPublishWithoutLogin", new MemoryPersistence());
    
            MqttConnectOptions connOpts = new MqttConnectOptions();
            client.connect(connOpts);
            Assert.fail("could connect with no credentials!");
    
            client.disconnect();
            logger.info("end trying to force a connection without login");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @Test(groups = {"testMQTTPublish"},dependsOnGroups = {"addSchema"})
    public void testMQTTPublish() {
        boolean succ = false;
        
        try {
            MqttClient client = new MqttClient("tcp://localhost:1884", "testMQTTPublish", new MemoryPersistence());
    
            MqttConnectOptions connOpts = new MqttConnectOptions();
            String pass = CommonBase.clientAdmin.getPassword();
            Assert.assertNotNull(pass);
            connOpts.setPassword(pass.toCharArray());
            connOpts.setUserName(CommonBase.clientAdmin.getUserName());
            client.connect(connOpts);
            client.subscribeWithResponse("#", this);
    
            logger.info("Connected");
            ContentResponse response = CommonBase.clientUser.getResponse(CommonBase.clientUser.createAuthedPostRequest("/device/add", new JSONObject().put("uuid", "multi2").put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 100)).put("schema", "https://kosmos-lab.de/schema/MultiSensor.json").put("scopes", new JSONObject().put("write", "mqttest:write").put("del", "mqttest:del").put("read", "mqttest:read"))));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
            logger.info("MQTT device add done");
            client.publish("kosmos/multi2/set",new JSONObject().put("currentEnvironmentTemperature",19).toString().getBytes(StandardCharsets.UTF_8),1,false);
            Device d = CommonBase.controller.getDevice("multi2");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",19,10000l),"MQTT set failed");
            client.publish("kosmos/multi2/set",new JSONObject().put("currentEnvironmentTemperature",13).toString().getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT pub 10");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",13,10000l),"MQTT set failed");
    
            client.publish("kosmos/multi2/currentEnvironmentTemperature/set","15".getBytes(StandardCharsets.UTF_8),1,false);
            logger.info("MQTT pub 11");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",15,10000l),"MQTT set by key failed");
            succ = true;
            client.disconnect();
        }
        catch (Exception ex) {
            if ( !succ) {
                Assert.fail("failed to connect",ex);
            }
            ex.printStackTrace();
        }
    }
    
    public void testWithScopes() {
        boolean succ = false;
        
        try {
            MqttClient client = new MqttClient("tcp://localhost", "MQTTTest2", new MemoryPersistence());
            
            MqttConnectOptions connOpts = new MqttConnectOptions();

            String pass = CommonBase.clientAdmin.getPassword();
            Assert.assertNotNull(pass);
            connOpts.setPassword(pass.toCharArray());
            connOpts.setUserName(CommonBase.clientUser.getUserName());
            client.connect(connOpts);
            client.subscribeWithResponse("#", this);
            MqttClient client2 = new MqttClient("tcp://localhost", "MQTTTest3", new MemoryPersistence());
    
            MqttConnectOptions connOpts2 = new MqttConnectOptions();
            String pass2 = CommonBase.clientUser2.getPassword();
            Assert.assertNotNull(pass);
            connOpts2.setPassword(pass2.toCharArray());
            connOpts2.setUserName(CommonBase.clientUser2.getUserName());
            client2.connect(connOpts2);
            client2.subscribeWithResponse("#", this);
            logger.info("Connected");
            ContentResponse response = CommonBase.clientUser.getResponse(CommonBase.clientUser.createAuthedPostRequest("/device/add", new JSONObject().put("uuid", "multi3").put("state", new JSONObject().put("currentEnvironmentTemperature", 25).put("humidityLevel", 100)).put("schema", "https://kosmos-lab.de/schema/MultiSensor.json")));
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), 204, "Device add did fail!");
            client.publish("kosmos/multi3/set",new JSONObject().put("currentEnvironmentTemperature",19).toString().getBytes(StandardCharsets.UTF_8),1,false);
            Device d = CommonBase.controller.getDevice("multi2");
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",19,10000l),"MQTT set failed");
            client.publish("kosmos/multi3/set",new JSONObject().put("currentEnvironmentTemperature",13).toString().getBytes(StandardCharsets.UTF_8),1,false);
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",13,10000l),"MQTT set failed");
            client2.publish("kosmos/multi3/set",new JSONObject().put("currentEnvironmentTemperature",14).toString().getBytes(StandardCharsets.UTF_8),1,false);
            Assert.assertFalse(CommonBase.waitForValue(d,"currentEnvironmentTemperature",14,10000l),"MQTT set NOT failed");
            client.publish("kosmos/multi3/currentEnvironmentTemperature/set","15".getBytes(StandardCharsets.UTF_8),1,false);
            Assert.assertTrue(CommonBase.waitForValue(d,"currentEnvironmentTemperature",15,10000l),"MQTT set by key failed");
            succ = true;
            client.disconnect();
        }
        catch (Exception ex) {
            if ( !succ) {
                Assert.fail("failed to connect",ex);
            }
            ex.printStackTrace();
        }
    }
}
