package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.KosmoSController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Event;
import de.kosmos_lab.platform.rules.RulesService;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.smarthome.EventInterface;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.enums.SecurityIn;
import de.kosmos_lab.web.annotations.enums.SecurityType;
import de.kosmos_lab.web.annotations.info.AsyncInfo;
import de.kosmos_lab.web.annotations.info.Contact;
import de.kosmos_lab.web.annotations.info.Info;
import de.kosmos_lab.web.annotations.info.License;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.security.SecuritySchema;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.WebSocketEndpoint;
import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.server.WebSocketCreator;
import de.kosmos_lab.web.server.WebSocketService;
import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

@Info(description = "# Kosmos Platform openApi HTTP API \n" +
        "This is the OpenAPI 3.0 specifaction for KosmoS, please always use the latest documentation found in your installation on [/doc/openapi.html](/doc/openapi.html) in your installation.\n" +
        "Please make sure you are logged in if you want to try to execute any request to the server.\n" +
        "You can simply login with the form injected to the top of the page.\n" +
        "(Almost) all POST requests with simple a datatype for parameters can be used either with parameters in query or a JSONObject in the request body. Exceptions are more complex datatypes like JSONObjects themselves (for example for /schema/add).\n" +
        "## YAML specification\n" +
        "You can find the newest openapi specification on [/doc/openapi.yaml](/doc/openapi.yaml) or [/doc/openapi.json](/doc/openapi.json).\n" +
        "The human readable description of the openapi can be found [/doc/openapi.html](/doc/openapi.html).\n" +
        "You can find the newest asyncapi specification on [/doc/asyncapi.yaml](/doc/asyncapi.yaml) or [/doc/asyncapi.json](/doc/asyncapi.json).\n" +
        "The human readable description of the asyncapi can be found [/doc/asyncapi.html](/doc/asyncapi.html).\n" +
        "**NOTE**: These files are automatically generated and also contain your plugins etc.",

        title = "KosmoS openApi",
        version = "filled-by-code",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
        contact = @Contact(name = "Jan Janssen", email = "Jan.Janssen@dfki.de")
)
@AsyncInfo(
        description = "# KosmoS ASynchron API\n" +
                "## KosmoS async webservice\n" +
                "All operations tagged with \"KosmoS\" are part of the default websocket and mqtt service.\n" +
                "## YAML specification\n" +
                "You can find the newest openapi specification on [/doc/openapi.yaml](/doc/openapi.yaml) or [/doc/openapi.json](/doc/openapi.json).\n" +
                "The human readable description of the openapi can be found [/doc/openapi.html](/doc/openapi.html).\n" +
                "You can find the newest asyncapi specification on [/doc/asyncapi.yaml](/doc/asyncapi.yaml) or [/doc/asyncapi.json](/doc/asyncapi.json).\n" +
                "The human readable description of the asyncapi can be found [/doc/asyncapi.html](/doc/asyncapi.html).\n" +
                "**NOTE**: These files are automatically generated and also contain your plugins etc.",
        title = "KosmoS asyncApi",
        version = "filled-by-code",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
        contact = @Contact(name = "Jan Janssen", email = "Jan.Janssen@dfki.de")
)
@ObjectSchema(
        componentName = "groupNameID",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the group",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The ID of the group",
                                type = SchemaType.INTEGER,
                                required = true

                        )
                )
        }
)
@ObjectSchema(
        componentName = "event",
        properties = {
                @SchemaProperty(
                        name = "type",
                        schema = @Schema(
                                description = "The type of the event",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "value",
                        schema = @Schema(
                                description = "The actual payload of the event,can be a anything the sender needs to send with an event",

                                required = true

                        )
                )
        }
)
@ObjectSchema(
        componentName = "deviceEvent",
        properties = {
                @SchemaProperty(
                        name = "type",
                        schema = @Schema(
                                description = "The type of the event",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "value",
                        schema = @Schema(
                                description = "The actual payload of the event,can be a anything the sender needs to send with an event",

                                required = false

                        )
                ),
                @SchemaProperty(
                        name = "uuid",
                        schema = @Schema(
                                description = "The UUID of the device this event belongs to, events will only be visible for users with read access to the device",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
        }
)
@ObjectSchema(
        componentName = "scopeNameID",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the scope",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The ID of the scope",
                                type = SchemaType.INTEGER,
                                required = true

                        )
                )
        }
)
@ObjectSchema(
        componentName = "userNameID",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the user",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The ID of the user",
                                type = SchemaType.INTEGER,
                                required = true

                        )
                )
        }
)
@ObjectSchema(
        componentName = "objectID",
        properties = {
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The ID",
                                type = SchemaType.INTEGER,
                                required = true

                        )
                )
        }
)
@ObjectSchema(
        componentName = "nameID",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The ID",
                                type = SchemaType.INTEGER,
                                required = true

                        )
                )
        }
)
@Schema(name = "username", type = SchemaType.STRING, description = "The name of a user")

@Schema(name = "userID", type = SchemaType.INTEGER, description = "The ID of a user")
@Schema(name = "userName", type = SchemaType.STRING, description = "The name of a user")
@Schema(name = "scopeID", type = SchemaType.INTEGER, description = "The ID of a scope")
@Schema(name = "scopeName", type = SchemaType.STRING, description = "The name of a scope")
@Schema(name = "groupID", type = SchemaType.INTEGER, description = "The ID of a group")
@Schema(name = "groupName", type = SchemaType.STRING, description = "The name of a group")
@de.kosmos_lab.web.annotations.servers.Server(description = "Current Host", url = "http://${host}")
@de.kosmos_lab.web.annotations.servers.Server(description = "Local Test", url = "http://localhost:18080")
@de.kosmos_lab.web.annotations.servers.Server(description = "Production", url = "https://example.cloud:18081")
@SecuritySchema(
        componentName = "bearerAuth",

        description = "contains a JSON Web Tokens (JWT) obtainable from #post-/user/login",
        type = SecurityType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecuritySchema(
        componentName = "basicAuth",
        description = "basic auth is also allowed for all requests",
        type = SecurityType.HTTP,
        scheme = "basic"
)
@SecuritySchema(
        componentName = "secret",
        name = "token",
        description = "Contains a secret known to both parties",
        type = SecurityType.APIKEY,
        in = SecurityIn.QUERY
)

public class KosmoSWebServer extends WebServer implements CommandInterface, EventInterface {
    private final IController controller;

    protected KosmoSWebSocketService webSocketService = null;

    public KosmoSWebServer(IController controller) throws Exception {
        super();
        this.controller = controller;

        prepare();
        start();
        controller.addCommandInterface(this);
    }

    @Override
    public void prepare() {
        super.prepare();

        this.findServlets(new String[]{"de.kosmos_lab.platform.web"}, KosmoSServlet.class, WebSocketService.class);

        try {
            JettyWebSocketServlet websocketServlet;
            this.webSocketService = new KosmoSWebSocketService(this, controller);
            WebSocketEndpoint wse = webSocketService.getClass().getAnnotation(WebSocketEndpoint.class);
            if (wse != null) {
                websocketServlet = new JettyWebSocketServlet() {
                    @Override
                    protected void configure(JettyWebSocketServletFactory factory) {
                        factory.setIdleTimeout(Duration.ofSeconds(60));
                        factory.setCreator(new WebSocketCreator(webSocketService, null));
                    }
                };

                wsPaths.add(wse.path());
                context.addServlet(new ServletHolder(websocketServlet), this.webSocketService.getClass().getAnnotation(WebSocketEndpoint.class).path());
            }


            if (getRulesService() != null) {
                wse = getRulesService().getClass().getAnnotation(WebSocketEndpoint.class);
                if (wse != null) {
                    websocketServlet = new JettyWebSocketServlet() {
                        @Override
                        protected void configure(JettyWebSocketServletFactory factory) {
                            factory.setIdleTimeout(Duration.ofSeconds(60));
                            factory.setCreator(new WebSocketCreator(getRulesService(), null));
                        }
                    };


                    context.addServlet(new ServletHolder(websocketServlet), wse.path());
                    //wsservices.remove(getRulesService().getClass());


                    wsPaths.add(wse.path());
                }
            }


            // Add WebSocket endpoint to jakarta.websocket layer


        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }


    @Override
    public HttpServlet create(Class<? extends HttpServlet> servlet, ApiEndpoint api) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (api.userLevel() >= 0) {

            return servlet.getConstructor(KosmoSWebServer.class, IController.class, int.class).newInstance(this, controller, api.userLevel());
        } else {
            try {
                return servlet.getConstructor(KosmoSWebServer.class, IController.class).newInstance(this, controller);
            } catch (NoSuchMethodException ex) {
                try {
                    return servlet.getConstructor(KosmoSWebServer.class).newInstance(this);
                } catch (NoSuchMethodException exx) {
                    return servlet.getConstructor(WebServer.class).newInstance(this);
                }
            }
        }
    }

    @Override
    public WebSocketService create(Class<? extends WebSocketService> servlet, WebSocketEndpoint api) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            return servlet.getConstructor(KosmoSWebServer.class, IController.class).newInstance(this, controller);
        } catch (NoSuchMethodException ex) {
            try {
                return servlet.getConstructor(KosmoSWebServer.class).newInstance(this);
            } catch (NoSuchMethodException exx) {
                return servlet.getConstructor(WebServer.class).newInstance(this);
            }
        }
    }

    /*public void createServlet(Class<? extends HttpServlet> servlet) {
        ApiEndpoint api = servlet.getAnnotation(ApiEndpoint.class);
        if (api != null) {
            try {
                if (paths.contains(api.path())) {
                    logger.warn("did find path {} again?", api.path());
                    return;
                }
                if (api.load()) {
                    logger.info("creating web servlet {} ", servlet.getName());

                    HttpServlet s = create(servlet,api);


                    context.addServlet(new ServletHolder(s), api.path());
                    loadedServlets.add(servlet);
                    paths.add(api.path());
                    logger.info("registered web servlet {} ", s.getClass());
                    return;
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }*/

    @Override
    public JSONObject getConfig() {
        return controller.getConfig().getJSONObject("webserver");
    }

    public void findServlets(String[] namespaces, Class<? extends HttpServlet> baseServletClass, Class<? extends WebSocketService> baseSocketClass) {
        super.findServlets(namespaces, baseServletClass, baseSocketClass);

        try {
            for (Class<? extends KosmoSServlet> c : controller.getPluginManager().getClassesFor(KosmoSServlet.class)) {
                if (!servlets.contains(c)) {
                    if (!servclasses.contains(c.getName())) {
                        servclasses.add(c.getName());

                        logger.info("PMM found KosmoSServlet: {}", c.getName());
                        servlets.add(c);
                    }
                }
            }
        } catch (org.reflections.ReflectionsException ex) {

        }
        try {
            for (Class<? extends WebSocketService> c : controller.getPluginManager().getClassesFor(WebSocketService.class)) {
                if (!wsservices.contains(c)) {
                    if (!wssclasses.contains(c.getName())) {
                        wssclasses.add(c.getName());
                        logger.info("PMM found WebSocketService: {}", c.getName());
                        wsservices.add(c);
                    }
                }
            }
        } catch (org.reflections.ReflectionsException ex) {

        }
    }

    // Convenience method to create and configure a ContextHandler.

    public IController getIController() {
        return this.controller;
    }


    public RulesService getRulesService() {
        return this.controller.getRulesService();
    }


    @Override
    public String getSourceName() {
        return "HTTPApi";
    }


    @Override
    public void deviceAdded(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {

    }

    @Override
    public void deviceRemoved(@Nullable CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {

    }

    @Override
    public void deviceUpdate(@Nullable CommandInterface from, @Nonnull Device device, @Nullable String key, @Nonnull CommandSourceName source) {

    }


    @Override
    public String toString() {
        return "WebServer";
    }


    public KosmoSWebSocketService getWebSocketService() {
        return webSocketService;
    }

    @Override
    public String replaceHostName(String text, String host) {
        String[] h = host.split(":");
        String hostname = host;
        String port = "";
        if (h.length == 2) {
            hostname = h[0];
            port = h[1];
        }
        text = text.replace("mqtt://${host}", String.format("mqtt://%s:%d", hostname, ((KosmoSController) controller).getMQTT().getPort()));

        text = text.replace("${host}", String.format("%s:%s", hostname, port));

        return text;
    }

    @Override
    public void eventFired(@Nullable EventInterface from, @Nonnull Event event) {
        //can be ignored here
    }
}
