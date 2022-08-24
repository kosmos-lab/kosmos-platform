package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.rules.RulesService;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.enums.SecurityIn;
import de.kosmos_lab.web.annotations.enums.SecurityType;
import de.kosmos_lab.web.annotations.info.Contact;
import de.kosmos_lab.web.annotations.info.Info;
import de.kosmos_lab.web.annotations.info.License;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.security.SecuritySchema;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.server.OpenApiParser;
import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.server.WebSocketCreator;
import de.kosmos_lab.web.server.WebSocketService;
import de.kosmos_lab.web.server.servlets.BaseServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

@Info(description = "# Kosmos Platform Synchron HTTP API \n" +
        "### [Asyncron WS/MQTT Documentation](async.html) \n" +
        "This is the OpenAPI 3.0 specifaction for KosmoS, it can be found on https://kosmos-lab.de/doc/openapi.yaml \n" +
        "Please make sure you are logged in if you want to try to execute any request to the server.\n" +
        "You can simply login with the form injected to the top of the page.\n" +
        "(Almost) all POST requests with simple a datatype for parameters can be used either with parameters in query or a JSONObject in the request body. Exceptions are more complex datatypes like JSONObjects themselves (for example for /schema/add).",
        title = "KosmoS OpenAPI",
        version = "filled-by-code",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
        contact = @Contact(name = "Jan Janssen", email = "Jan.Janssen@dfki.de"))
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
        bearerFormat = "JWT",
        scheme = "basic"
)
@SecuritySchema(
        componentName = "secret",
        name = "token",
        description = "Contains a secret known to both parties",
        type = SecurityType.APIKEY,
        in = SecurityIn.QUERY
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
@Schema(name = "userID", type = SchemaType.INTEGER, description = "The ID of a user")
@Schema(name = "userName", type = SchemaType.STRING, description = "The name of a user")
@Schema(name = "scopeID", type = SchemaType.INTEGER, description = "The ID of a scope")
@Schema(name = "scopeName", type = SchemaType.STRING, description = "The name of a scope")
@Schema(name = "groupID", type = SchemaType.INTEGER, description = "The ID of a group")
@Schema(name = "groupName", type = SchemaType.STRING, description = "The name of a group")
@de.kosmos_lab.web.annotations.servers.Server(description = "Current Host", url = "http://none/ #ignore this")
@de.kosmos_lab.web.annotations.servers.Server(description = "Local Test", url = "http://localhost:18080")
@de.kosmos_lab.web.annotations.servers.Server(description = "Production", url = "https://example.cloud:18081")
public class KosmoSWebServer extends WebServer implements CommandInterface {
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
        OpenApiParser.serverClass = KosmoSWebServer.class;
        this.findServlets(new String[]{"de.kosmos_lab.platform.web"}, KosmoSServlet.class, WebSocketService.class);

        try {
            JettyWebSocketServlet websocketServlet;
            this.webSocketService = new KosmoSWebSocketService(this, controller);

            websocketServlet = new JettyWebSocketServlet() {
                @Override
                protected void configure(JettyWebSocketServletFactory factory) {
                    factory.setIdleTimeout(Duration.ofSeconds(60));
                    factory.setCreator(new WebSocketCreator(webSocketService, null));
                }
            };
            wsservices.remove(KosmoSWebSocketService.class);
            context.addServlet(new ServletHolder(websocketServlet), this.webSocketService.getClass().getAnnotation(ServerEndpoint.class).value());
            if (getRulesService() != null) {
                websocketServlet = new JettyWebSocketServlet() {
                    @Override
                    protected void configure(JettyWebSocketServletFactory factory) {
                        factory.setIdleTimeout(Duration.ofSeconds(60));
                        factory.setCreator(new WebSocketCreator(getRulesService(), null));
                    }
                };
                context.addServlet(new ServletHolder(websocketServlet), this.getRulesService().getClass().getAnnotation(ServerEndpoint.class).value());
                wsservices.remove(getRulesService().getClass());
            }


            // Add WebSocket endpoint to jakarta.websocket layer


        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }


    public void createWebSocketService(Class<? extends WebSocketService> c) {
        //logger.info("found: WebSocketService: {}", c.getName());
        ServerEndpoint endpoint = c.getAnnotation(ServerEndpoint.class);

        if (endpoint != null) {
            logger.info("found: WebSocketService: {} endpoint {}", c.getName(), endpoint.value());
            try {

                WebSocketService service = c.getConstructor(KosmoSWebServer.class, IController.class).newInstance(this, controller);

                JettyWebSocketServlet websocketServlet = new JettyWebSocketServlet() {
                    @Override
                    protected void configure(JettyWebSocketServletFactory factory) {
                        factory.setIdleTimeout(Duration.ofSeconds(60));
                        factory.setCreator(new WebSocketCreator(service, null));
                    }
                };

                context.addServlet(new ServletHolder(websocketServlet), endpoint.value());

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void createServlet(Class<? extends HttpServlet> servlet) {
        ApiEndpoint api = servlet.getAnnotation(ApiEndpoint.class);
        if (api != null) {
            try {
                if (paths.contains(api.path())) {
                    logger.warn("did find path {} again?", api.path());
                    return;
                }
                if (api.load()) {
                    logger.info("creating web servlet {} ", servlet.getName());

                    HttpServlet s;
                    if (api.userLevel() >= 0) {

                        s = servlet.getConstructor(KosmoSWebServer.class, IController.class, int.class).newInstance(this, controller, api.userLevel());
                    } else {
                        try {
                            s = servlet.getConstructor(KosmoSWebServer.class, IController.class).newInstance(this, controller);
                        } catch (NoSuchMethodException ex) {
                            try {
                                s = servlet.getConstructor(KosmoSWebServer.class).newInstance(this);
                            } catch (NoSuchMethodException exx) {
                                s = servlet.getConstructor(WebServer.class).newInstance(this);
                            }
                        }
                    }

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

    }

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


}
