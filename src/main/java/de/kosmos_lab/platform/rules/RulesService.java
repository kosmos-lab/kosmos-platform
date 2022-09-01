package de.kosmos_lab.platform.rules;

import de.kosmos_lab.web.doc.openapi.WebSocketEndpoint;
import de.kosmos_lab.web.server.JWT;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.utils.KosmosFileUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.eclipse.jetty.websocket.api.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@WebSocketEndpoint(path = "/kreews")
@WebSocket
public class RulesService extends de.kosmos_lab.web.server.WebSocketService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("RulesService");
    private final HashMap<Session, IUser> webSocketClients = new HashMap<>();
    private final IController controller;
    private final HashMap<IUser, RulesExecuter> executors = new HashMap<>();
    private final String ruleDirectory;
    private final File ruleDirectoryFile;
    private final HashMap<Session, IUser> logins = new HashMap<Session, IUser>();


    public RulesService(IController controller) {
        this(controller, "rules");
    }

    @SuppressFBWarnings("DM_EXIT")
    public RulesService(@Nonnull IController controller,@Nonnull  String dir) {
        super(controller.getWebServer());
        logger.info("started RulesService");
        this.controller = controller;
        this.ruleDirectory = dir + "/rules/";
        this.ruleDirectoryFile = new File(ruleDirectory);
        if (!ruleDirectoryFile.exists()) {
            if (!ruleDirectoryFile.mkdirs()) {
                logger.warn("could not create gesture folder \"{}\" - exiting", ruleDirectoryFile);
                System.exit(1);
            }
        }
        if (!controller.currentlyInTesting()) {
            File[] files = ruleDirectoryFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".py")) {
                        try {
                            logger.info("found Rule: {}", f.getName());
                            String uid = f.getName();
                            uid = uid.substring(0, uid.length() - 3);
                            try {
                                IUser u = controller.getUser(Integer.parseInt(uid));
                                startExecutor(u);
                            } catch (UserNotFoundException ex) {

                            }
                        } catch (Exception e) {
                            logger.error("exception: ", e);

                        }

                    }
                }
            }
        }
    }


    @Override
    @OnWebSocketConnect
    public void addWebSocketClient(@Nonnull Session sess) {

        synchronized (webSocketClients) {
            this.webSocketClients.put(sess, null);
        }
    }

    public void broadcastToUser(@Nonnull IUser user,@Nonnull String text) {
        //better safe than sorry here
        synchronized (webSocketClients) {
            for (Map.Entry<Session, IUser> entry : this.webSocketClients.entrySet()) {
                if (user == entry.getValue()) {
                    try {
                        entry.getKey().getRemote().sendString(text);
                    } catch (org.eclipse.jetty.io.EofException ex) {
                        //Nothing here
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void delWebSocketClient(@Nonnull Session sess) {
        synchronized (webSocketClients) {
            this.webSocketClients.remove(sess);
        }
    }

    public @Nonnull String getName(@Nonnull IUser user) {
        return this.ruleDirectory + user.getUUID().getLeastSignificantBits();
    }

    public @Nonnull File getRuleDir() {
        return this.ruleDirectoryFile;
    }

    public @Nonnull String getRuleDirString() {
        return this.ruleDirectory;
    }

    public @CheckForNull String getXML(@Nonnull IUser user) {

        File file = new File(getName(user) + ".xml");
        if (file.exists()) {
            return KosmosFileUtils.readFile(file);
        }
        return null;

    }

    public void newLogMessage(@Nonnull IUser user,@Nonnull  String line) {
        logger.info("new log for {}: {}", user.getUUID().getLeastSignificantBits(), line);
        broadcastToUser(user, (new JSONObject().put("type", "log").put("value", line)).toString());
    }

    @Override
    @OnWebSocketMessage
    public void onWebSocketMessage(@Nonnull Session sess,@Nonnull  String message) {
        if (message.equalsIgnoreCase("ping")) {
            try {
                sess.getRemote().sendString("pong");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (message.startsWith("user/auth:")) {


            try {
                JSONObject j = new JSONObject(message.substring(10).trim());
                IUser u = this.controller.tryLogin(j.getString("user"), j.getString("pass"));

                if (u != null) {
                    webSocketClients.put(sess, u);
                    logins.put(sess, u);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }


            return;
        }
        if (message.startsWith("Bearer ")) {
            message = message.substring(6).trim();

            try {
                JSONObject s = controller.getJwt().verify(message);
                IUser u = controller.getUser(s.getString("name"));
                if (u != null) {
                    webSocketClients.put(sess, u);
                    logins.put(sess, u);
                    try {
                        sess.getRemote().sendString("auth successful");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        sess.getRemote().sendString("auth failed");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JWT.JWTVerifyFailed jwtVerifyFailed) {
                jwtVerifyFailed.printStackTrace();
            }


            return;
        }

    }

    public void savePython(@Nonnull IUser user,@Nonnull  String code) {

        KosmosFileUtils.writeToFile(new File(getName(user) + ".py"), code);
        if (!controller.currentlyInTesting()) {
            /*RulesExecuter re = this.executors.get(user);
            if (re != null) {
                re.restart();
            } else {
                startExecutor(user);
            }*/
            restartExecutor(user);
        }
    }

    public void saveXML(@Nonnull IUser user,@Nonnull  String xml) {
        KosmosFileUtils.writeToFile(new File(getName(user) + ".xml"), xml);

    }

    private void startExecutor(@Nonnull IUser user) {
        if (!controller.currentlyInTesting()) {
            RulesExecuter re = new RulesExecuter(this, user);
            this.executors.put(user, re);
            re.start();
        }
    }

    public void restartExecutor(@Nonnull IUser user) {
        RulesExecuter re = this.executors.get(user);
        if (re != null) {
            re.restart();
        }
        else {
            re = new RulesExecuter(this, user);
            this.executors.put(user, re);
            re.start();
        }
    }

    public void restartAllExecutors() {
        for (RulesExecuter re : this.executors.values()) {
            if (re != null) {
                re.restart();
            }
        }
    }

}
