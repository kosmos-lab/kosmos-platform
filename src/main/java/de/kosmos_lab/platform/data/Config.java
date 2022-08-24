package de.kosmos_lab.platform.data;

import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.utils.StringFunctions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SuppressFBWarnings("DM_EXIT")
public class Config extends JSONObject {
    private File file;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("Config");


    public Config(@Nonnull File f) throws IOException {
        super(new JSONTokener(new FileReader(f, StandardCharsets.UTF_8)));
        this.file = f;
        init();
    }

    public Config() {

        init();

    }

    public void setFile(@Nonnull  File f) {
        this.file = f;
    }

    public void save() {
        KosmosFileUtils.writeToFile(file, this.toString(4));
    }


    private void init() {
        JSONObject sql = optJSONObject("sql");
        if (sql == null) {
            sql = new JSONObject();
            put("sql", sql);
        }
        String jdbcurl = sql.optString("url", null);
        if (jdbcurl == null) {
            sql.put("url", "jdbc:sqlite:./db/database.sqlite");
            File f = new File("db/");
            if (!f.exists()) {

                if (!f.mkdirs()) {
                    logger.warn("could not create database folder \"{}\" - exiting",f);
                    System.exit(1);
                }
            }
        }
        Object o = sql.opt("maxConnections");
        if (o == null) {
            sql.put("maxConnections", 10);
        }

        String pepper = optString("pepper", null);
        if (pepper == null) {
            pepper = StringFunctions.generateRandomKey();
            this.put("pepper", pepper);
        }
        String jwt = optString("jwt", null);
        if (jwt == null) {
            jwt = StringFunctions.generateRandomKey();
            this.put("jwt", jwt);
        }
        /*JSONArray users = optJSONArray("users");
        if (users == null) {
            users = new JSONArray();
            this.put("users", users);
        }
        
        if (users.length() == 0) {
            JSONObject user = new JSONObject();
            user.put("user", "admin");
            String salt = Controller.generateRandomKey();
            user.put("salt", salt);
            user.put("level", 100);
            user.put("hash", Controller.getSaltedAndPepperdHash("pass", salt, pepper));
            
            users.put(user);
        }*/


        JSONObject webserver = optJSONObject("webserver");
        if (webserver == null) {
            webserver = new JSONObject();
            this.put("webserver", webserver);
        }
        try {
            int port = webserver.getInt("port");
        } catch (JSONException e) {
            webserver.put("port", 18080);
        }
        JSONObject mqtt = optJSONObject("mqtt");
        if (mqtt == null) {
            mqtt = new JSONObject();
            this.put("mqtt", mqtt);
        }
        try {
            int port = mqtt.getInt("port");
        } catch (JSONException e) {
            mqtt.put("port", 1883);
        }
        try {
            String host = mqtt.getString("host");
        } catch (JSONException e) {
            mqtt.put("host", "0.0.0.0");
        }
        if (!has("statelog")) {
            put("statelog", new JSONObject());
        }
        JSONObject statelog = getJSONObject("statelog");

        if (!statelog.has("whitelist")) {
            statelog.put("whitelist", new JSONArray());
        }

        if (!statelog.has("blacklist")) {
            statelog.put("blacklist", new JSONArray());
        }
    }

}
