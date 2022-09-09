package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.platform.persistence.Constants;
import de.kosmos_lab.utils.StringFunctions;
import de.kosmos_lab.web.client.websocket.SimpleWebSocketEndpoint;
import de.kosmos_lab.web.client.websocket.WebSocketTestClient;
import de.kosmos_lab.web.server.WebServer;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestEvents {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("Test");


    @Test
    public void testWebSocketClient() {
        String id_targetList = "targetList";
        String id_auth = "auth";
        String id_show = "show";
        try {

            //prepare the websocket uri
            String wsUri = String.format("%s/ws", CommonBase.baseUrl.replace("http://", "ws://"));

            //create admin websocket
            WebSocketTestClient adminWebSocket = new WebSocketTestClient(new URI(wsUri));
            //create a listener that shows all messages
            adminWebSocket.addMessageHandler(Pattern.compile(".*"), new SimpleWebSocketEndpoint.RegexMessageHandler() {
                public void handleMessage(String message, Matcher matcher) {

                    logger.info("WS Admin: {}", message);
                }
            });
            //create a listener for targetList

            String testpw = StringFunctions.generateRandomKey();
            //will actually never happen - but better safe than sorry
            while (testpw.equals(CommonBase.clientAdmin.getPassword())) {
                testpw = StringFunctions.generateRandomKey();
            }

            adminWebSocket.addMessageHandler("auth successful", message -> {
                //set the variable targetList to the current value
                adminWebSocket.set("authed", "1");
            });
            adminWebSocket.addMessageHandler("auth failed", message -> {
                //set the variable targetList to the current value
                adminWebSocket.set("authed", "0");
            });
            // auth with false password
            adminWebSocket.addMessageHandler(Constants.websocketSplitPattern, (message, matcher) -> {
                //set the variable targetList to the current value
                //JSONObject json = new JSONObject(matcher.group(2));
                adminWebSocket.set(matcher.group(1),matcher.group(2));
            });
            adminWebSocket.sendMessage(String.format("user/login:%s",new JSONObject().put("user", CommonBase.clientAdmin.getUserName()).put("pass", testpw)));
            Assert.assertTrue(CommonBase.waitForValue(adminWebSocket.getObjects(), "authed", "0", 1000), "did not get auth failed back");
            JSONObject event = new JSONObject().put("type","test").put("value","7");
            ContentResponse response = CommonBase.clientAdmin.getResponse("/event", HttpMethod.POST, event);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE);
            Assert.assertFalse(CommonBase.waitForValue(adminWebSocket.getObjects(), "event", event, 1000), "did get event back before login!");
            adminWebSocket.sendMessage(String.format("user/login:%s",new JSONObject().put("user", CommonBase.clientAdmin.getUserName()).put("pass", CommonBase.clientAdmin.getPassword())));
            Assert.assertTrue(CommonBase.waitForValue(adminWebSocket.getObjects(), "authed", "1", 1000), "did not get auth success back");
            event = new JSONObject().put("type","test").put("value","10");
            response = CommonBase.clientAdmin.getResponse("/event", HttpMethod.POST, event);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), WebServer.STATUS_NO_RESPONSE);
            Assert.assertTrue(CommonBase.waitForValue(adminWebSocket.getObjects(), "event", event, 1000), "did not get event back");
            /*Assert.assertFalse(adminWebSocket.getObjects().has(id_targetList), "targetList should have been missing");
            adminWebSocket.sendMessage(new JSONObject().put("type", KioskConstants.WS_Type_auth).put("username", TestBase.clientAdmin.getUserName()).put("password", TestBase.clientAdmin.getPassword()).toString());
            Assert.assertTrue(CommonBase.waitForValue(adminWebSocket.getObjects(), id_auth, KioskConstants.WS_Type_authSuccess, 1000), "could not auth!");

            //auth as an admin

            //wait for an empty listTargets
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_targetList, new JSONArray(), 1000), "did not get an empty targetList after auth");
            Assert.assertTrue(adminWebSocket.getObjects().has(id_targetList), "targetList should not have been missing - ");
            //get the targetlist synchronous
            ContentResponse response = TestBase.clientAdmin.getResponse("/kiosk/listTargets", HttpMethod.GET);
            Assert.assertNotNull(response, "response was null");
            Assert.assertEquals(response.getStatus(), 200, "response was not 200!");
            JSONArray array = new JSONArray(response.getContentAsString());
            //JSONArray array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            //check that its not empty
            Assert.assertNotNull(array, "kiosk/listTargets returned null");
            // list should be empty
            Assert.assertEquals(array.length(), 0, "targets is not an empty Array");
            // open websocket
            final WebSocketTestClient clientWebSocket = new WebSocketTestClient(new URI(wsUri));

            // add listener for all events
            clientWebSocket.addMessageHandler(Pattern.compile(".*"), new SimpleWebSocketEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    //System.out.println(String.format("WS client: %s",message));
                    logger.info("WS Client: {}", message);
                }
            });

            // set target of the clientSocket to test1
            clientWebSocket.sendMessage(new JSONObject().put("type", "setTarget").put("value", "test1").toString());
            // wait for the adminsocket to get the new listTargets events with a
            CommonBase.waitForValue(adminWebSocket.getObjects(), "listTargets", new JSONArray().put("test1"), 1000);
            // check
            array = CommonBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            Assert.assertNotNull(array);
            Assert.assertEquals(array.length(), 1, "listTargets is not equal to 1");
            Assert.assertEquals(array.getString(0), "test1");
            JSONObject json = new JSONObject().put("type", "show-image").put("value", "https://foo.bar/test.png").put("target", "test1");

            clientWebSocket.addMessageHandler(Pattern.compile(".*\"type\":\"show\\-.*"), new SimpleWebSocketEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(String.format("WS show: %s", message));
                    JSONObject json = new JSONObject(message);
                    clientWebSocket.set(id_show, json.getString("value"));
                }
            });
            TestBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            Assert.assertTrue(TestBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.png", 5000));
            json = new JSONObject().put("type", "show-video").put("value", "https://foo.bar/test.mp4").put("target", "test1");
            TestBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            Assert.assertTrue(CommonBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.mp4", 5000));
            json = new JSONObject().put("type", "show-video").put("value", "https://foo.bar/test.avi").put("target", "test2");
            CommonBase.clientAdmin.postJSONObject2("/kiosk/sendMessage", json);
            //reset message to an empty message
            clientWebSocket.set(id_show, "");
            //change own Target id
            clientWebSocket.sendMessage(new JSONObject().put("type", "setTarget").put("value", "test2").toString());
            //wait for the show to come in
            Assert.assertTrue(TestBase.waitForValue(clientWebSocket.getObjects(), id_show, "https://foo.bar/test.avi", 5000));


            //check the targets synchronous
            array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            //
            Assert.assertNotNull(array, "the result of kiosk/targets was null!");
            Assert.assertEquals(array.length(), 1, "listTargets is not equal to 1");
            Assert.assertEquals(array.getString(0), "test2", "the only entry of the list should be test2");
            try {
                clientWebSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Thread.sleep(500);
            array = TestBase.clientAdmin.getJSONArray("/kiosk/listTargets");
            Assert.assertNotNull(array);
            Assert.assertEquals(array.length(), 0, "listTargets is not empty");
            Assert.assertTrue(TestBase.waitForValue(adminWebSocket.getObjects(), id_targetList, new JSONArray(), 5000));



             */
            Thread.sleep(1);

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }

}
