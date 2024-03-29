package de.kosmos_lab.platform.smarthome.ha;

import jakarta.websocket.ClientEndpointConfig;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HomeAssistantHTTPClient extends HttpClient {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("HomeAssistantHTTPClient");

    private final String user;
    private final String pass;
    private final String base;
    boolean stopped = false;
    private String token;

    public HomeAssistantClient getEndpoint() {
        return endpoint;
    }

    private HomeAssistantClient endpoint;

    public HomeAssistantHTTPClient(String base, String user, String pass) {
        this.base = base;
        this.user = user;
        this.pass = pass;
        this.setConnectTimeout(5000L);


        /*this.setMaxConnectionsPerDestination(10);
        this.setMaxRequestsQueuedPerDestination(100);*/

        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void connect() {
        HomeAssistantHTTPClient c = this;
        (new Thread() {
            public void run() {
                while (!stopped) {
                    try {
                        //this.devices.clear();
                        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

                        ClientManager client = ClientManager.createClient();
                        endpoint = new HomeAssistantClient(c);
                        client.connectToServer(endpoint, cec, new URI(getBase().replace("https:", "wss:").replace("http:", "ws:") + "/api/websocket"));

                        while (!endpoint.stopped) {
                            Thread.sleep(100);
                        }

                    } catch (Exception e) {
                        logger.error("Exception!", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Exception!", e);
                    }
                }
            }
        }).start();


    }

    private Request createRequest(String url, HttpMethod method) {

        if (!url.startsWith("http")) {
            url = base + url;
        }
        logger.trace("creating request for {}", url);
        Request request = newRequest(url);
        request.method(method);
        request.agent("KosmoS Client");
        return request;
    }

    public void disconnect() {
        this.stopped = true;
        this.endpoint.stop();
    }

    public JSONObject getVars() {
        return this.endpoint.getVars();
    }

    public void setVar(String key, Object value) {
        this.endpoint.setVar(key, value);
    }

    public String getBase() {
        return this.base;
    }

    public String getPass() {
        return this.pass;
    }

    /**
     * get the response for a given result, if 401 is returned the jwt will be renewed and it will be tried again
     *
     * @param request the request to parse
     *
     * @return
     */
    private ContentResponse getResponse(Request request) {
        ContentResponse response = null;
        try {

            response = request.send();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();

        }
        return response;

    }

    public String getToken() {
        return this.token;
    }

    public Object getVar(String key) {
        return this.endpoint.getVar(key);
    }

    public void setToken(String access_token) {
        this.token = access_token;
    }

    public String getUser() {
        return this.user;
    }

    public HomeAssistantClient getWebSocket() {
        return this.endpoint;
    }

    public String post(String url, HashMap<String, Object> parameters) {
        Request request = createRequest(url, HttpMethod.POST);
        MultiPartContentProvider multiPart = new MultiPartContentProvider();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            multiPart.addFieldPart(entry.getKey(), new StringContentProvider(entry.getValue().toString()), null);
        }
        request.content(multiPart);
        return (getResponse(request).getContentAsString());
    }

    public JSONObject postJSON(String url, JSONObject json) {
        Request request = createRequest(url, HttpMethod.POST);
        request.timeout(120, TimeUnit.SECONDS);
        request.idleTimeout(120, TimeUnit.SECONDS);
        if (json != null) {
            request.body(new StringRequestContent(json.toString()));
            //request.content(new StringContentProvider(json.toString()), "application/json");

        }
        if (token != null) {

            request.headers(httpFields -> httpFields.add("Authorization", "Bearer " + token).add("Content-Type", "application/json"));

        } else {
            request.headers(httpFields -> httpFields.add("Content-Type", "application/json"));
        }
        logger.info("posting json to {} (token):{}", request.getURI(), token, json);


        ContentResponse r = getResponse(request);
        if (r != null) {
            try {
                return new JSONObject(r.getContentAsString());
            } catch (JSONException ex) {
                logger.error("HTTP Status {}:{}", r.getStatus(), r.getContentAsString());
                logger.error("Exception", ex);
            }

        }
        return null;
    }

    public void sendCommand(JSONObject command, HomeAssistantEventConsumer consumer) {
        if (this.endpoint != null) {
            this.endpoint.sendCommand(command, consumer);
        } else {
            logger.error("NO ENDPOINT!!");
        }
    }

    public void unstop() {
        this.stopped = false;
    }

    public boolean waitForValue(String key, Object expected, long waittime) {

        return endpoint.waitForValue(key, expected, waittime);
    }

    public void waitForInit() throws InterruptedException {
        this.endpoint.initLatch.await();
    }
}
