package de.kosmos_lab.kosmos.platform.web;

import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.enums.SecurityIn;
import de.dfki.baall.helper.webserver.annotations.enums.SecurityType;
import de.dfki.baall.helper.webserver.annotations.info.Contact;
import de.dfki.baall.helper.webserver.annotations.info.Info;
import de.dfki.baall.helper.webserver.annotations.info.License;
import de.dfki.baall.helper.webserver.annotations.media.ObjectSchema;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.annotations.security.SecuritySchema;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.rules.RulesService;
import de.kosmos_lab.kosmos.platform.smarthome.CommandInterface;
import de.kosmos_lab.kosmos.platform.smarthome.CommandSourceName;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashSet;

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
@de.dfki.baall.helper.webserver.annotations.servers.Server(description = "Current Host", url = "http://none/ #ignore this")
@de.dfki.baall.helper.webserver.annotations.servers.Server(description = "Local Test", url = "http://localhost:18080")
@de.dfki.baall.helper.webserver.annotations.servers.Server(description = "Production", url = "https://example.cloud:18081")
public class WebServer implements CommandInterface {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("WebServer");

    private final IController controller;
    private final Server server;

    private final int port;
    private final ServletContextHandler context;
    HashSet<Class<? extends KosmoSServlet>> loadedServlets = new HashSet<>();
    private KosmoSWebSocketService webSocketService = null;
    private boolean stopped = false;

    public WebServer(IController controller) {
        this.controller = controller;


        int maxThreads = 10;
        int minThreads = 2;
        int idleTimeout = 120;

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        JSONObject webserverConfig = controller.getConfig().getJSONObject("webserver");
        this.server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        this.port = webserverConfig.getInt("port");
        connector.setPort(port);
        server.addConnector(connector);
        ContextHandlerCollection handlers = new ContextHandlerCollection();

        //this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        this.context = servletContextHandler;
        servletContextHandler.setContextPath("/");
        //context.setContextPath("/");
        ServletHolder staticFiles = new ServletHolder("default", new DefaultServlet());

        staticFiles.setInitParameter("resourceBase", "./web/");
        staticFiles.setInitParameter("dirAllowed", "false");

        servletContextHandler.addServlet(staticFiles, "/*");

        //dont judge :p
        //reflections magic to work around the fact that embedded jetty does not want to read the annotations by itself..

        HashSet<String> paths = new HashSet<>();
        HashSet<Class<? extends KosmoSServlet>> servlets = new HashSet<>();
        HashSet<Class<? extends WebSocketService>> wsservices = new HashSet<>();

        HashSet<String> wssclasses = new HashSet<>();
        HashSet<String> servclasses = new HashSet<>();

        HashSet<Class<? extends Exception>> thrown = new HashSet<>();
        for (Reflections r : new Reflections[]{new Reflections(""), new Reflections("de.kosmos_lab.kosmos.platform.web")}) {
            for (Class<? extends Exception> c : r.getSubTypesOf(Exception.class)) {
                if (!wsservices.contains(c)) {

                    if (c.getAnnotation(ApiResponse.class) != null) {
                        logger.info("Reflections found Exception: {}", c.getName());
                        thrown.add(c);
                    }

                }
            }
            for (Class<? extends WebSocketService> c : r.getSubTypesOf(WebSocketService.class)) {
                if (!wsservices.contains(c)) {
                    if (!wssclasses.contains(c.getName())) {
                        wssclasses.add(c.getName());
                        logger.info("Reflections found WebSocketService: {}", c.getName());
                        wsservices.add(c);

                    }
                }
            }
            for (Class<? extends KosmoSServlet> c : r.getSubTypesOf(KosmoSServlet.class)) {
                if (!servlets.contains(c)) {
                    if (!servclasses.contains(c.getName())) {
                        servclasses.add(c.getName());

                        logger.info("Reflections found KosmoSServlet: {}", c.getName());
                        servlets.add(c);
                    }
                }
            }
            for (Class<? extends WebSocketService> c : controller.getPluginManager().getClassesFor(WebSocketService.class)) {
                if (!wsservices.contains(c)) {
                    if (!wssclasses.contains(c.getName())) {
                        wssclasses.add(c.getName());
                        logger.info("PMM found WebSocketService: {}", c.getName());
                        wsservices.add(c);
                    }
                }
            }
            for (Class<? extends KosmoSServlet> c : controller.getPluginManager().getClassesFor(KosmoSServlet.class)) {
                if (!servlets.contains(c)) {
                    if (!servclasses.contains(c.getName())) {
                        servclasses.add(c.getName());

                        logger.info("PMM found KosmoSServlet: {}", c.getName());
                        servlets.add(c);
                    }
                }
            }
            for (Class c : r.getTypesAnnotatedWith(ServerEndpoint.class)) {
                if (c.isAssignableFrom(WebSocketService.class)) {
                    if (!wsservices.contains(c)) {
                        if (!wssclasses.contains(c.getName())) {
                            wssclasses.add(c.getName());
                            logger.info("Annotations found WebSocketService: {}", c.getName());
                            wsservices.add(c);
                        }
                    }
                }
                if (WebSocketService.class.isAssignableFrom(c)) {
                    if (!wsservices.contains(c)) {
                        if (!wssclasses.contains(c.getName())) {
                            wssclasses.add(c.getName());
                            logger.info("Annotations2 found WebSocketService: {}", c.getName());
                            wsservices.add(c);
                        }
                    }
                }
            }
            for (Class c : r.getTypesAnnotatedWith(ApiEndpoint.class)) {
                if (c.isAssignableFrom(KosmoSServlet.class)) {
                    if (!servlets.contains(c)) {
                        if (!servclasses.contains(c.getName())) {
                            servclasses.add(c.getName());

                            logger.info("Annotations found KosmoSServlet: {}", c.getName());
                            servlets.add(c);
                        }
                    }
                }
                if (KosmoSServlet.class.isAssignableFrom(c)) {
                    if (!servlets.contains(c)) {
                        if (!servclasses.contains(c.getName())) {
                            servclasses.add(c.getName());

                            logger.info("Annotations2 found KosmoSServlet: {}", c.getName());
                            servlets.add(c);
                        }
                    }
                }
            }
            for (Class c : r.getTypesAnnotatedWith(jakarta.servlet.annotation.WebServlet.class)) {
                if (c.isAssignableFrom(KosmoSServlet.class)) {
                    if (!servlets.contains(c)) {
                        if (!servclasses.contains(c.getName())) {
                            servclasses.add(c.getName());

                            logger.info("Annotations3 found KosmoSServlet: {}", c.getName());
                            servlets.add(c);
                        }
                    }
                }
                if (KosmoSServlet.class.isAssignableFrom(c)) {
                    if (!servlets.contains(c)) {
                        if (!servclasses.contains(c.getName())) {
                            servclasses.add(c.getName());

                            logger.info("Annotations4 found KosmoSServlet: {}", c.getName());
                            servlets.add(c);
                        }
                    }
                }
            }
        }
        for (Class<? extends KosmoSServlet> c : servlets) {
/*            if (loadedServlets.contains(c)) {
                continue;
            }*/
            ApiEndpoint api = c.getAnnotation(ApiEndpoint.class);
            if (api != null) {
                try {
                    if (paths.contains(api.path())) {
                        logger.warn("did find path {} again?",api.path());
                        continue;
                    }
                    KosmoSServlet s;
                    if (api.userLevel() >= 0) {
                        s = c.getConstructor(WebServer.class, IController.class, int.class).newInstance(this, controller, api.userLevel());
                    } else {
                        s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    }

                    servletContextHandler.addServlet(new ServletHolder(s), api.path());
                    loadedServlets.add(c);
                    paths.add(api.path());
                    logger.info("registered web servlet {} ", s.getClass());
                    continue;

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
            jakarta.servlet.annotation.WebServlet f = c.getAnnotation(jakarta.servlet.annotation.WebServlet.class);
            if (f != null) {
                try {
                    KosmoSServlet s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    for (String url : f.urlPatterns()) {
                        if (paths.contains(url)) {
                            continue;
                        }
                        servletContextHandler.addServlet(new ServletHolder(s), url);
                        paths.add(url);
                        loadedServlets.add(c);
                        logger.info("registered web servlet {} ", s.getClass());
                    }

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

        /*
            if (loadedServlets.contains(c)) {
                continue;
            }
            logger.info("Plugin manager found: KosmoSServlet: {}", c.getName());
            ApiEndpoint api = c.getAnnotation(ApiEndpoint.class);
            if (api != null) {
                if (paths.contains(api.path())) {
                    continue;
                }
                try {
                    KosmoSServlet s;
                    if (api.userLevel() >= 0) {
                        s = c.getConstructor(WebServer.class, IController.class, int.class).newInstance(this, controller, api.userLevel());
                    } else {
                        s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    }

                    servletContextHandler.addServlet(new ServletHolder(s), api.path());
                    loadedServlets.add(c);
                    paths.add(api.path());
                    logger.info("PMM registered web servlet {} ",s.getClass());
                    continue;

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
            jakarta.servlet.annotation.WebServlet f = c.getAnnotation(jakarta.servlet.annotation.WebServlet.class);
            if (f != null) {
                try {
                    KosmoSServlet s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    for (String url : f.urlPatterns()) {
                        if (paths.contains(url)) {
                            continue;
                        }
                        servletContextHandler.addServlet(new ServletHolder(s), url);
                        loadedServlets.add(c);
                        paths.add(url);

                    }
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

        }*/


        try {
            JettyWebSocketServlet websocketServlet;
            // Initialize jakarta.websocket layer
            this.webSocketService = new KosmoSWebSocketService(this, controller);

            //ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            websocketServlet = new JettyWebSocketServlet() {
                @Override
                protected void configure(JettyWebSocketServletFactory factory) {

                    factory.setCreator(new KosmosWebSocketCreator(webSocketService, null));
                }
            };
            wsservices.remove(KosmoSWebSocketService.class);
            servletContextHandler.addServlet(new ServletHolder(websocketServlet), this.webSocketService.getClass().getAnnotation(ServerEndpoint.class).value());
            if (getRulesService() != null) {
                websocketServlet = new JettyWebSocketServlet() {
                    @Override
                    protected void configure(JettyWebSocketServletFactory factory) {
                        factory.setIdleTimeout(Duration.ofSeconds(60));
                        factory.setCreator(new KosmosWebSocketCreator(getRulesService(), null));
                    }
                };
                servletContextHandler.addServlet(new ServletHolder(websocketServlet), this.getRulesService().getClass().getAnnotation(ServerEndpoint.class).value());
                wsservices.remove(getRulesService().getClass());
            }


            // Add WebSocket endpoint to jakarta.websocket layer


            for (Class<? extends WebSocketService> c : wsservices) {
                //logger.info("found: WebSocketService: {}", c.getName());
                ServerEndpoint endpoint = c.getAnnotation(ServerEndpoint.class);

                if (endpoint != null) {
                    logger.info("found: WebSocketService: {} endpoint {}", c.getName(), endpoint.value());
                    try {

                        WebSocketService service = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                         /*websocketServlet = new JettyWebSocketServlet() {
                            @Override
                            protected void configure(JettyWebSocketServletFactory factory) {
                                factory.addMapping("/", (req, res) -> service);
                            }
                        };*/
                        websocketServlet = new JettyWebSocketServlet() {
                            @Override
                            protected void configure(JettyWebSocketServletFactory factory) {
                                factory.setIdleTimeout(Duration.ofSeconds(60));
                                factory.setCreator(new KosmosWebSocketCreator(service, null));
                            }
                        };

                        servletContextHandler.addServlet(new ServletHolder(websocketServlet), endpoint.value());

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
            JettyWebSocketServletContainerInitializer.configure(servletContextHandler, null);


            handlers.addHandler(servletContextHandler);

            server.setHandler(handlers);
            OpenApiParser.create(this.loadedServlets);

            server.start();
            //server.dump(System.err);


        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        controller.addCommandInterface(this);

    }

    // Convenience method to create and configure a ContextHandler.
    private static ContextHandler createContextHandler(String contextPath, Handler wrappedHandler) {
        ContextHandler ch = new ContextHandler(contextPath);
        ch.setHandler(wrappedHandler);
        ch.clearAliasChecks();
        ch.setAllowNullPathInfo(true);
        return ch;
    }

    public HashSet<Class<? extends KosmoSServlet>> getLoadedServlets() {
        return this.loadedServlets;
    }

    public IController getIController() {
        return this.controller;
    }

    public int getPort() {
        return port;
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

    public void stop() {
        try {
            this.stopped = true;
            this.server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "WebServer";
    }


    public KosmoSWebSocketService getWebSocketService() {
        return webSocketService;
    }

    public void addStaticFile(File f, String name) {
        ServletHolder staticFiles = new ServletHolder("default", new DefaultServlet());

        staticFiles.setInitParameter("resourceBase", "./web/");

        context.addServlet(staticFiles, "/*");

    }

    public boolean isStopped() {
        return stopped;
    }
}
