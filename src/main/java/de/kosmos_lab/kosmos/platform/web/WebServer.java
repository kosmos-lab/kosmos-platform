package de.kosmos_lab.kosmos.platform.web;

import de.dfki.baall.helper.webserver.JWT;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.UserNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants;
import de.kosmos_lab.kosmos.platform.rules.RulesService;
import de.kosmos_lab.kosmos.platform.smarthome.CommandInterface;
import de.kosmos_lab.kosmos.platform.smarthome.CommandSourceName;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;

public class WebServer implements CommandInterface {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("WebServer");

    private final IController controller;
    private final Server server;

    private final int port;
    private final ServletContextHandler context;
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

        this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        ServletHolder staticFiles = new ServletHolder("default", new DefaultServlet());

        staticFiles.setInitParameter("resourceBase", "./web/");
        staticFiles.setInitParameter("dirAllowed", "false");

        context.addServlet(staticFiles, "/*");


        //dont judge :p
        //reflections magic to work around the fact that embedded jetty does not want to read the annotations by itself..
        Reflections r = new Reflections("de.kosmos_lab.kosmos");
        for (Class<? extends KosmoSServlet> c : r.getSubTypesOf(KosmoSServlet.class)) {
            javax.servlet.annotation.WebServlet f = c.getAnnotation(javax.servlet.annotation.WebServlet.class);
            if (f != null) {
                try {
                    KosmoSServlet s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    for (String url : f.urlPatterns()) {
                        context.addServlet(new ServletHolder(s), url);
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
        for (Class<? extends KosmoSServlet> c : controller.getPluginManager().getClassesFor(KosmoSServlet.class)) {

            logger.info("Plugin manager found: KosmoSServlet: {}", c.getName());
            javax.servlet.annotation.WebServlet f = c.getAnnotation(javax.servlet.annotation.WebServlet.class);
            if (f != null) {
                try {
                    KosmoSServlet s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                    for (String url : f.urlPatterns()) {
                        context.addServlet(new ServletHolder(s), url);
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


        server.setHandler(context);

        try {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            this.webSocketService = new KosmoSWebSocketService(controller, this);
            ServerEndpoint a = webSocketService.getClass().getAnnotation(ServerEndpoint.class);
            wscontainer.addEndpoint(ServerEndpointConfig.Builder
                    .create(KosmoSWebSocketEndpoint.class, a.value()) // the endpoint url
                    .configurator(new KosmoSWebSocketEndpointConfiguration(webSocketService))
                    .build());
            if (this.getRulesService() != null) {
                a = this.getRulesService().getClass().getAnnotation(ServerEndpoint.class);
                wscontainer.addEndpoint(ServerEndpointConfig.Builder
                        .create(KosmoSWebSocketEndpoint.class, a.value()) // the endpoint url
                        .configurator(new KosmoSWebSocketEndpointConfiguration(this.getRulesService()))
                        .build());
            }

            for (Class<? extends WebSocketService> c : controller.getPluginManager().getClassesFor(WebSocketService.class)) {

                logger.info("Plugin manager found: WebSocketService: {}", c.getName());
                ServerEndpoint endpoint = c.getAnnotation(ServerEndpoint.class);
                logger.info("Plugin manager found: WebSocketService: {} endpoint {}", c.getName(), endpoint.value());
                if (endpoint != null) {
                    try {
                        WebSocketService s = c.getConstructor(WebServer.class, IController.class).newInstance(this, controller);
                        wscontainer.addEndpoint(ServerEndpointConfig.Builder
                                .create(KosmoSWebSocketEndpoint.class, endpoint.value()) // the endpoint url
                                .configurator(new KosmoSWebSocketEndpointConfiguration(s))
                                .build());
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
            server.start();
            //server.dump(System.err);


        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        controller.addCommandInterface(this);

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
