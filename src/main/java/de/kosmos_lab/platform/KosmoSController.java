package de.kosmos_lab.platform;

import de.kosmos_lab.platform.camera.subtitles.data.SubtitleEntry;
import de.kosmos_lab.platform.client.HTTPClient;
import de.kosmos_lab.platform.data.*;
import de.kosmos_lab.platform.exceptions.*;
import de.kosmos_lab.platform.gesture.GestureProvider;
import de.kosmos_lab.platform.mqtt.MQTTBroker;
import de.kosmos_lab.platform.persistence.Constants;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.platform.persistence.Constants.RunMode;
import de.kosmos_lab.platform.persistence.IPersistence;
import de.kosmos_lab.platform.persistence.KosmoSPersistence;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import de.kosmos_lab.platform.rules.RulesService;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.smarthome.EventInterface;
import de.kosmos_lab.platform.smarthome.SmartHomeInterface;
import de.kosmos_lab.platform.smarthome.ha.HomeAssistantHTTPClient;
import de.kosmos_lab.platform.utils.DataFactory;
import de.kosmos_lab.platform.utils.KosmoSHelper;
import de.kosmos_lab.platform.utils.SchemaComparator;
import de.kosmos_lab.platform.utils.TimeFunctions;
import de.kosmos_lab.platform.web.IAuthProvider;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.KosmoSWebSocketService;
import de.kosmos_lab.utils.HashFunctions;
import de.kosmos_lab.utils.JSONFunctions;
import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.utils.StringFunctions;
import de.kosmos_lab.utils.Wildcard;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.web.server.JWT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Kosmos controller.
 */
@SuppressFBWarnings("DM_EXIT")
public class KosmoSController implements IController {


    /**
     * The constant logger.
     */
    protected static final Logger logger = LoggerFactory.getLogger("KosmoSController");
    private static final Pattern extraneousKeyPattern = Pattern.compile("extraneous key \\[(?<keys>.*)\\] is not permitted");
    private final static long subDefaultTime = 10000;
    /**
     * custom JWT class to use
     */
    private final JWT jwt;
    private final KosmosPluginManager pluginManager;
    private final RunMode runMode;
    private final KosmoSPersistence persistence;
    private final ConcurrentHashMap<Device, Boolean> logByDevice = new ConcurrentHashMap<>();
    private final HashSet<String> logWhiteListFilter = new HashSet<>();
    private final HashSet<String> logBlackListFilter = new HashSet<>();
    /**
     * cache of the CommandSourceNames
     */
    public ConcurrentHashMap<String, CommandSourceName> cacheSource = new ConcurrentHashMap<>();
    /**
     * The Fallback schema.
     */
    public DataSchema fallbackSchema = null;
    /**
     * the camera map
     */
    public ConcurrentHashMap<String, ICamera> cameras = new ConcurrentHashMap<>();
    public ConcurrentHashMap<ICamera, IUser> recordings = new ConcurrentHashMap<>();
    /**
     * the config object to use
     */
    protected Config config;
    /**
     * the jetty based webserver
     */
    protected KosmoSWebServer webServer;
    /**
     * The Rules service.
     */
    protected RulesService rulesService;
    /**
     * list of smart environments
     */
    Set<SmartHomeInterface> smartEnvironments = ConcurrentHashMap.newKeySet();
    /**
     * Scope Cache <Name:String,Scope>
     */
    ConcurrentHashMap<String, Scope> scopes = new ConcurrentHashMap<>();
    /**
     * Scope Cache <ID:Integer,Scope>
     */
    ConcurrentHashMap<Integer, Scope> scopesI = new ConcurrentHashMap<>();
    /**
     * Scope Cache <Name:String,Group>
     */
    ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    /**
     * Scope Cache <ID:Integer,Group>
     */
    ConcurrentHashMap<Integer, Group> groupsI = new ConcurrentHashMap<>();
    /**
     * schema Cache <URI:String,DataSchema>
     */
    ConcurrentHashMap<String, DataSchema> schemas = new ConcurrentHashMap<>();
    /**
     * user Cache <username:String,User>
     */
    ConcurrentHashMap<String, IUser> users = new ConcurrentHashMap<>();
    /**
     * user Cache <ID:Integer,User>
     */
    ConcurrentHashMap<UUID, IUser> usersI = new ConcurrentHashMap<>();
    /**
     * user Cache <ID:Integer,User>
     */
    ConcurrentHashMap<Integer, IUser> usersID = new ConcurrentHashMap<>();
    /**
     * device Cache <UUID:String,Device>
     */
    ConcurrentHashMap<String, Device> devices = new ConcurrentHashMap<>();
    /**
     * HashSet of UpdateTarget that need to get updates
     */
    Set<CommandInterface> commandInterfaces = ConcurrentHashMap.newKeySet();
    Set<EventInterface> eventInterfaces = ConcurrentHashMap.newKeySet();
    Set<IAuthProvider> authProviders = ConcurrentHashMap.newKeySet();

    /**
     * The Gesture provider.
     */
    GestureProvider gestureProvider;
    ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);
    private MQTTBroker mqttBroker;
    private boolean stopped = false;

    /**
     * Instantiates a new Kosmo s controller.
     */
    public KosmoSController() {
        this(new File("config/config.json"));
    }

    /**
     * Instantiates a new Kosmo s controller.
     *
     * @param configFile the config file
     */
    public KosmoSController(File configFile) {
        this(configFile, RunMode.NORMAL);
    }

    /**
     * Instantiates a new Kosmo s controller.
     *
     * @param configFile the config file
     * @param runMode    the runMode
     */
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    public KosmoSController(@Nonnull File configFile, RunMode runMode) {

        logger.info("Starting KosmoSController with config {} in runMode {}", configFile, runMode);
        this.runMode = runMode;

        try {
            this.config = new Config(configFile);

        } catch (FileNotFoundException e) {
            this.config = new Config();
            config.setFile(configFile);
        } catch (IOException e) {
            logger.error("Exception while reading config file: ", e);
            System.exit(1);
        }
        config.save();
        this.jwt = new JWT(config.getString("jwt"));


        this.persistence = new KosmoSPersistence(
                this.config.getJSONObject("sql"),
                this,
                false);
        JSONObject logging = config.optJSONObject("logging");
        if (logging != null) {
            JSONArray list = logging.optJSONArray("blacklist");
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    try {
                        this.logBlackListFilter.add(list.getString(i));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            list = logging.optJSONArray("whitelist");
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    try {
                        this.logWhiteListFilter.add(list.getString(i));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }

        this.persistence.init();

        //this.commandInterfaces.add(persistence);

        //if we are not in runMode
        if (runMode == RunMode.TEST) {
            String ruleDir = getFileString("rules");

            for (String f : new String[]{"kosmos.py"}) {
                try {
                    File file = new File(
                            String.format("rules/%s", f));
                    if (file.exists()) {
                        FileUtils.copyFile(file, new File(
                                String.format("%s/%s", ruleDir, f)));
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }


        }
        gestureProvider = new GestureProvider(true, getFileString("gestures"));
        this.rulesService = new RulesService(this, getFileString("rules"));
        if (runMode == RunMode.NORMAL) {


            //create default users
            if (this.users.isEmpty()) {
                String USERS = KosmoSHelper.getEnv("USERS");
                if (USERS != null) {
                    try {
                        JSONArray arr = new JSONArray(USERS);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            this.addUser(obj.getString("username"), obj.getString("password"), obj.optInt("level", 1));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                }

                File f = getFile("config/users.json");
                if (f.exists()) {
                    JSONArray arr = new JSONArray(KosmosFileUtils.readFile(f));
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject obj = arr.getJSONObject(i);
                            this.addUser(obj.getString("username"), obj.getString("password"), obj.optInt("level", 1));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }


                if (this.users.isEmpty()) {
                    this.addUser("admin", "pass", 100);
                    this.addUser("user", "pass", 1);
                    this.addUser("user2", "pass", 1);
                    this.addUser("context", "pass", 1);
                }


            }
            //import the schemas
            //if (this.schemas.isEmpty()) {
            File schemaDir = new File("schema");
            if (schemaDir.exists()) {
                File[] fileList = schemaDir.listFiles();
                if (fileList != null) {
                    for (File schemaFile : fileList) {
                        if (schemaFile.getName().endsWith(".json")) {
                            try {
                                DataSchema schema = DataFactory.getSchema(schemaFile);
                                if (schema != null && schema.getSchema() != null) {
                                    /*try {

                                        if (schemaFile.lastModified() > System.currentTimeMillis() - 3600000) {

                                            DataSchema s = this.getSchema(schema.getSchema().getId());
                                            if (s != null) {
                                                this.getPersistence().delSchema(schema);
                                            }
                                        } else {
                                            continue;
                                        }

                                    } catch (SchemaNotFoundException ex) {
//not a problem here
                                    }*/
                                    try {


                                        //this.getSchema(schema.getSchema().getId());
                                        this.getPersistence().getSchema(schema.getSchema().getId());


                                    } catch (NotFoundInPersistenceException ex) {
                                        this.getPersistence().addSchema(schema);
                                    }

                                }
                            } catch (NotObjectSchemaException ex) {
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            //}
            try {
                File dfile = getFile("config/devices.json");
                if (dfile.exists()) {
                    JSONArray devices = new JSONArray(KosmosFileUtils.readFile(dfile));
                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject o = devices.getJSONObject(i);
                        try {
                            this.parseAddDevice(this.persistence, o, this.getSource("HTTPApi:user"), getUser("user"), false);
                        } catch (DeviceAlreadyExistsException e) {

                            JSONObject state = o.getJSONObject("state");
                            if (state != null && state.keySet().size() > 0) {
                                try {
                                    this.parseSet(this.persistence, o.getString("uuid"), state, this.getSource("HTTPApi:user"), getUser("user"));
                                } catch (NoAccessToScope noAccessToScope) {
                                    noAccessToScope.printStackTrace();
                                }
                            }
                        }
                    }


                } else {
                    String CREATE_ALL_DEVICE = KosmoSHelper.getEnv("CREATE_ALL_DEVICE");
                    if (KosmoSHelper.getEnvBool("CREATE_ALL_DEVICE")) {
                        DataSchema ds;
                        try {
                            ds = this.getSchema("https://kosmos-lab.de/schema/All.json");
                            if (ds == null) {
                                logger.info("no DS?");
                            } else if (!ds.getRawSchema().has("title")) {
                                logger.info("no title?!");
                            } else if (ds.getRawSchema().has("examples")) {
                                JSONArray examples = ds.getRawSchema().getJSONArray("examples");
                                if (examples.length() > 0) {
                                    String uuid = ds.getSchema().getTitle() + "0";
                                    if (!this.devices.containsKey(uuid)) {


                                        JSONObject ex = examples.getJSONObject(0);
                                        JSONObject o = new JSONObject();
                                        o.put("state", ex);
                                        o.put("schema", ds.getSchema().getId());
                                        o.put("uuid", uuid);
                                        try {
                                            this.parseAddDevice(this.persistence, o, this.getSource("HTTPApi:admin"), this.getUser("admin"));
                                        } catch (DeviceAlreadyExistsException e) {
                                            e.printStackTrace();
                                        } catch (ParameterNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (SchemaNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (ValidationException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        logger.info("example already exists {}", uuid);
                                    }
                                }

                            } else {
                                logger.info("no examples?!");
                            }
                        } catch (SchemaNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    if (KosmoSHelper.getEnvBool("CREATE_DEVICES")) {
                        HashMap<String, Integer> perSchema = new HashMap<String, Integer>();
                        for (Device d : devices.values()) {
                            if (d.getSchema() != null) {
                                String id = d.getSchema().getId();
                                Integer amount = perSchema.get(id);
                                if (amount == null) {
                                    perSchema.put(id, 1);
                                } else {
                                    perSchema.put(id, 1 + amount);
                                }
                            }
                        }
                        IUser admin = getUser("admin");
                        if (admin == null) {
                            admin = this.users.values().iterator().next();
                        }
                        int min = 2;
                        String schema = "https://kosmos-lab.de/schema/Lamp.json";
                        Integer amnt = perSchema.get(schema);

                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_lamp_" + i);
                                JSONObject state = new JSONObject();
                                state.put("on", false);
                                obj.put("state", state);
                                obj.put("schema", schema);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException e) {
                                    //ignore this

                                } catch (ParameterNotFoundException e) {
                                    logger.error("Could not add Lamp because Parameter was not found!", e);
                                } catch (SchemaNotFoundException e) {
                                    logger.error("Could not add Lamp because Schema was not found!", e);
                                }
                            }
                        }
                        schema = "https://kosmos-lab.de/schema/DimmableLamp.json";
                        amnt = perSchema.get(schema);
                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_dim_lamp_" + i);
                                JSONObject state = new JSONObject();
                                state.put("on", false);
                                state.put("dimmingLevel", 0);
                                obj.put("schema", schema);
                                obj.put("state", state);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException e) {
                                    //ignore this

                                } catch (ParameterNotFoundException e) {
                                    logger.error("Could not add Dimmable Lamp because Parameter was not found!", e);
                                } catch (SchemaNotFoundException e) {
                                    logger.error("Could not add Dimmable Lamp because Schema was not found!", e);
                                }
                            }
                        }
                        schema = "https://kosmos-lab.de/schema/HSVLamp.json";
                        amnt = perSchema.get(schema);
                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_hsv_lamp_" + i);
                                JSONObject state = new JSONObject();
                                state.put("on", false);
                                state.put("hue", 0);
                                state.put("saturation", 0);
                                state.put("dimmingLevel", 0);
                                obj.put("state", state);
                                obj.put("schema", schema);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException e) {
                                    //ignore this

                                } catch (ParameterNotFoundException e) {
                                    logger.error("Could not add HSV Lamp because Parameter was not found!", e);
                                } catch (SchemaNotFoundException e) {
                                    logger.error("Could not add HSV Lamp because Schema was not found!", e);
                                }
                            }
                        }
                        schema = "https://kosmos-lab.de/schema/OccupancySensor.json";
                        amnt = perSchema.get(schema);
                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_occupancy_sensor_" + i);
                                JSONObject state = new JSONObject();
                                state.put("occupancy", false);

                                obj.put("state", state);
                                obj.put("schema", schema);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException e) {
                                    //ignore this

                                } catch (ParameterNotFoundException e) {
                                    logger.error("Could not add Occupancy Sensor because Parameter was not found!", e);
                                } catch (SchemaNotFoundException e) {
                                    logger.error("Could not add Occupancy Sensor because Schema was not found!", e);
                                }
                            }
                        }
                        schema = "https://kosmos-lab.de/schema/TemperatureSensor.json";
                        amnt = perSchema.get(schema);
                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_sensor_temp_" + i);
                                obj.put("schema", schema);
                                JSONObject state = new JSONObject();
                                state.put("currentEnvironmentTemperature", 21.5);

                                obj.put("state", state);
                                obj.put("schema", schema);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException e) {
                                    //ignore this

                                } catch (ParameterNotFoundException e) {
                                    logger.error("Could not add Temperature Sensor because Parameter was not found!", e);
                                } catch (SchemaNotFoundException e) {
                                    logger.error("Could not add Temperature Sensor because Schema was not found!", e);
                                }
                            }
                        }
                        schema = "https://kosmos-lab.de/schema/Heater.json";
                        amnt = perSchema.get(schema);
                        if (amnt == null || amnt < min) {
                            for (int i = 0; i < min; i++) {
                                JSONObject obj = new JSONObject();
                                obj.put("uuid", "virt_heater_" + i);
                                JSONObject state = new JSONObject();
                                state.put("heatingTemperatureSetting", 21.5);

                                obj.put("state", state);
                                obj.put("schema", schema);
                                try {
                                    this.parseAddDevice(this.persistence, obj, getSource("virtual"), admin);
                                } catch (DeviceAlreadyExistsException | ParameterNotFoundException |
                                         SchemaNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Exception while reading devices.json", e);
            }
        }
       /*
        File schemaDir = new File("schema");
        if (schemaDir.exists()) {
            for (File schemaFile : schemaDir.listFiles()) {
                if ( schemaFile.getName().endsWith(".json")) {
                    DataSchema schema = DataFactory.getSchema(schemaFile);
                    if (schema.getSchema() instanceof ObjectSchema) {
                        ObjectSchema os = (ObjectSchema)schema.getSchema();
                        this.getPersistence().addSchema(schema);
                    }
                }
            }
        }*/

        this.pluginManager = new KosmosPluginManager();
        for (Class cls : pluginManager.getAllClassesFor(IAuthProvider.class)) {
            try {
                logger.info("found IAuthProvider class {}",cls.getCanonicalName());
                //this.authProviders.add(cls.getI)

                //IAuthProvider p = ((cls.get) cls).getInstance(this);
                Method m = cls.getMethod("getInstance", IController.class);
                Object p = m.invoke(null, this);
                if (p != null) {
                    this.authProviders.add((IAuthProvider) p);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            this.webServer = new KosmoSWebServer(this);
        } catch (Exception e) {
            logger.error("Fatal exception while starting WebServer", e);
            System.exit(0);
            //throw new RuntimeException(e);

        }
        //addCommandInterface(webServer);

        try {
            this.mqttBroker = new MQTTBroker(this);
            //addCommandInterface(mqttBroker);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupHA();
        if (config.has("cameras")) {
            try {
                JSONArray camsJSON = config.getJSONArray("cameras");
                for (int i = 0; i < camsJSON.length(); i++) {
                    try {
                        JSONObject camJSON = camsJSON.getJSONObject(i);

                        String clazz = camJSON.getString("clazz");
                        if (clazz != null) {
                            Class<?> c = null;

                            try {
                                c = Class.forName(clazz);

                            } catch (ClassNotFoundException ex) {
                                //logger.error("exception while parsing camera {}", ex.getMessage(), ex);
                            }

                            if (c == null) {

                                try {
                                    c = pluginManager.getClass(clazz);
                                } catch (ClassNotFoundException ex) {
                                    //logger.error("exception while parsing camera {}", ex.getMessage(), ex);
                                }

                            }
                            if (c != null) {
                                if (ICamera.class.isAssignableFrom(c)) {
                                    ICamera cam = (ICamera) c.getConstructor(JSONObject.class).newInstance(camJSON);
                                    this.cameras.put(cam.getName(), cam);
                                } else {
                                    logger.error("Could NOT parse {} as ICamera", c.getName());

                                }
                            }
                            if (c == null) {
                                logger.error("could not find class {} for camera", clazz);
                            }
                        }
                    } catch (Exception ex) {
                        logger.error("exception while parsing camera {}", ex.getMessage(), ex);
                    }
                }
            } catch (Exception ex) {
                logger.error("exception while parsing cameras {}", ex.getMessage(), ex);
            }
        }
        logger.info("KosmoS startup complete");
    }

    /**
     * fix the json object to pass validation
     *
     * @param json
     * @param exception
     *
     * @return
     */
    private static JSONObject fixValidation(@Nonnull JSONObject json, @Nonnull ValidationException exception) {
        logger.error("trying to fix ValidationException {} {}", json, exception.getMessage());
        JSONObject exceptionjson = exception.toJSON();
        if (exceptionjson.has("keyword")) {
            String keyword = exceptionjson.getString("keyword");
            if (keyword.equals("additionalProperties")) {
                String message = exceptionjson.getString("message");
                if (message != null) {
                    Matcher m = extraneousKeyPattern.matcher(message);
                    if (m.matches()) {
                        for (String key : m.group("keys").split(",")) {
                            logger.warn("removing extraneous Key {} to fix JSONValidation", key);
                            json.remove(key);
                        }
                        return json;

                    }
                }

            }
        }
        return null;


    }

    /**
     * Short uuid string.
     *
     * @param input the input
     *
     * @return the string
     */
    public static String shortUUID(String input) {
        if (input.startsWith("light.")) {
            return input.substring(6);
        }
        if (input.startsWith("sensor.")) {
            return input.substring(7);
        }
        if (input.startsWith("binary_sensor.")) {
            return input.substring(14);
        }
        return input;

    }

    public static String getFileString(String name, RunMode mode) {

        switch (mode) {
            case TEST:
            case PLUGIN_TEST:
                return String.format("./_test/%s", name);
            case NORMAL:
                return String.format("./%s", name);

        }
        return null;
    }

    public static File getFile(String name, RunMode mode) {
        String dir = getFileString(name, mode);
        File f = new File(dir);
        f.getParentFile().mkdirs();
        return f;
    }

    @Override
    public void addDeviceText(Device device, String key, String value) {
        DeviceText deviceText = device.getText(key);
        if (deviceText == null) {
            deviceText = new DeviceText(device, key, value);
            //device.addText(deviceText);
            this.getPersistence().addDeviceText(deviceText);
        } else {
            deviceText.setValue(value);
            this.getPersistence().updateDeviceText(deviceText);
        }


    }

    @Override
    public void addEventInterface(EventInterface eventInterface) {
        this.eventInterfaces.add(eventInterface);

    }

    @Override
    public void fireEvent(Event event, EventInterface source) {
        for (EventInterface eventInterface : this.eventInterfaces) {
            if (eventInterface != source) {
                eventInterface.eventFired(source, event);
            }
        }
    }

    /**
     * adds a new Device, also adds it to persistence if wanted
     *
     * @param from         the from
     * @param device       the device
     * @param addPermanent should it be added to persistence?
     * @param source       the source
     */
    public void addDevice(@Nonnull CommandInterface from, @Nonnull Device device, boolean addPermanent, @Nonnull CommandSourceName source) {
        this.devices.put(device.getUniqueID(), device);
        this.getPersistence().fillDeviceScopes(device);
        this.getPersistence().fillTexts(device);
        for (CommandInterface t : this.commandInterfaces) {
            if (!addPermanent) {
                if (t instanceof IPersistence) {
                    //if this a persistence device, remove the old device if it exists
                    try {
                        ((IPersistence) t).delDevice(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            t.deviceAdded(from, device, source);
        }
    }

    /**
     * add a device to the system
     *
     * @param from         the UpdateTarget this originated from
     * @param device       the device to add
     * @param addPermanent should this be added as a permanent device to the persistence?
     */
    public void addDevice(@Nonnull CommandInterface from, @Nonnull Device device, boolean addPermanent) {
        addDevice(from, device, addPermanent, this.getSource(from.getSourceName()));
    }

    /**
     * add a device to the system
     *
     * @param from         the UpdateTarget this originated from
     * @param device       the device to add
     * @param addPermanent should this be added as a permanent device to the persistence?
     * @param source       Who/What added the device
     */
    public void addDevice(@Nonnull CommandInterface from, @Nonnull Device device, boolean addPermanent, @Nonnull String source) {
        addDevice(from, device, addPermanent, this.getSource(source));
    }

    /**
     * add a group to the system
     *
     * @param name name of the group
     * @param user the creator of the group (will be added as an admin)
     *
     * @return
     */
    public @Nonnull
    Group addGroup(@Nonnull String name, @Nonnull IUser user) throws GroupAlreadyExistsException {
        return this.getPersistence().addGroup(name, user);
    }

    /**
     * add an admin to a group
     *
     * @param group
     * @param user
     */
    public void addGroupAdmin(@Nonnull Group group, @Nonnull IUser user) {
        this.getPersistence().addGroupAdmin(group, user);

    }

    /**
     * add a user to the group
     *
     * @param group
     * @param user
     */
    public void addGroupUser(@Nonnull Group group, @Nonnull IUser user) {
        this.getPersistence().addGroupUser(group, user);

    }

    /**
     * add a Schema to persistence
     *
     * @param dataSchema
     */
    public void addSchema(@Nonnull DataSchema dataSchema) {
        this.getPersistence().addSchema(dataSchema);
    }

    /**
     * add a scope to the system
     *
     * @param name the name of the scope
     * @param user the owner of the scope
     *
     * @return
     */
    public Scope addScope(@Nonnull String name, @Nonnull IUser user) throws ScopeAlreadyExistsException {
        return this.getPersistence().addScope(name, user);
    }

    /**
     * add a user as an admin
     *
     * @param scope the scope
     * @param user  the user
     */
    public void addScopeAdmin(Scope scope, IUser user) {
        this.getPersistence().addScopeAdmin(scope, user);

    }

    /**
     * add a group with regular access to the scope
     *
     * @param scope
     * @param group
     */
    public void addScopeGroup(@Nonnull Scope scope, @Nonnull Group group) {
        this.getPersistence().addScopeGroup(scope, group);

    }

    public void addScopeAdminGroup(@Nonnull Scope scope, @Nonnull Group group) {
        this.getPersistence().addScopeAdminGroup(scope, group);

    }

    /**
     * add a user to the scope
     *
     * @param scope
     * @param user
     */
    public void addScopeUser(@Nonnull Scope scope, @Nonnull IUser user) {
        this.getPersistence().addScopeUser(scope, user);

    }

    /**
     * adds a smarthome to the system
     *
     * @param smartHomeInterface
     */
    public void addSmartHome(@Nonnull SmartHomeInterface smartHomeInterface) {
        this.smartEnvironments.add(smartHomeInterface);
        this.commandInterfaces.add(smartHomeInterface);

    }

    /**
     * create a new user
     *
     * @param username the username
     * @param password the password
     * @param level    the level
     */
    public void addUser(String username, String password, int level) {
        String salt = StringFunctions.generateRandomKey();

        addUser(new KosmoSUser(this, username, 0, level,
                hashSaltPepper(password, salt)
                , salt));
    }

    /**
     * add the given user to the persistence
     *
     * @param u
     */
    public void addUser(@Nonnull IUser u) {
        this.getPersistence().addUser(u);
        this.cacheUser(u);
    }

    @Override
    public void addCommandInterface(@Nonnull CommandInterface commandInterface) {
        this.commandInterfaces.add(commandInterface);
    }

    /**
     * add the device to the cache
     *
     * @param device
     */
    public void cacheDevice(@Nonnull Device device) {
        this.devices.put(device.getUniqueID(), device);

    }

    public void setDeviceLocation(@Nonnull String uuid, @Nonnull Device.Location loc) {
        try {
            Device d = this.getDevice(uuid);
            d.setLocation(loc);
        } catch (DeviceNotFoundException ex) {

        }

    }

    /**
     * add a group to the cache
     *
     * @param group
     */
    public void cacheGroup(@Nonnull Group group) {
        this.groups.put(group.getName(), group);
        this.groupsI.put(group.getID(), group);


    }

    /**
     * add the dataSchema to the cache
     *
     * @param dataSchema
     */
    public void cacheSchema(@Nonnull DataSchema dataSchema) {
        this.schemas.put(dataSchema.getSchema().getId(), dataSchema);
    }

    /**
     * cache a scope
     *
     * @param scope
     */
    public void cacheScope(@Nonnull Scope scope) {
        this.scopes.put(scope.getName(), scope);
        this.scopesI.put(scope.getID(), scope);


    }

    /**
     * adds the user to the cache
     *
     * @param user
     */
    public void cacheUser(@Nonnull IUser user) {
        this.users.put(user.getName().toLowerCase(), user);
        this.usersI.put(user.getUUID(), user);
        if (user instanceof KosmoSUser) {
            this.usersID.put(((KosmoSUser) user).getID(), user);
        }

    }

    @Override
    public void createSubs(@Nonnull String path, @Nonnull String filename, @Nonnull Calendar videoStart, @Nonnull Calendar videoEnd, @Nonnull List<LoggingRequest> uuids, long delta) {
        HashSet<String> uuidList = new HashSet<String>();
        HashMap<String, Set<String>> proplist = new HashMap<>();
        for (LoggingRequest request : uuids) {
            uuidList.add(request.getUuid());
            proplist.put(request.getUuid(), request.getProperties());
        }
        List<LogEntry> logs = getLogs(videoStart, videoEnd, uuidList);

        HashMap<String, List<SubtitleEntry>> uuidsubs = new HashMap<>();
        HashMap<String, SubtitleEntry> subbacklog = new HashMap<>();
        //iterate all the log entries
        for (LogEntry e : logs) {

            //check if there is already an entry with the given uuid
            SubtitleEntry lastentry = subbacklog.get(e.getUuid());
            if (lastentry != null) {
                //if there is another entry for this uuid, check if the display would overlap
                long t = e.getDate().getTime();
                if (lastentry.to > t) {
                    lastentry.to = t;
                }

            }
            JSONObject json = new JSONObject();
            for (String p : proplist.get(e.getUuid())) {
                json.put(p, e.getState().opt(p));
            }
            SubtitleEntry myEntry = new SubtitleEntry(e.getDate().getTime(), e.getDate().getTime() + subDefaultTime, json.toString(), e.getUuid());
            subbacklog.put(e.getUuid(), myEntry);
            List<SubtitleEntry> list = uuidsubs.get(e.getUuid());
            if (list == null) {
                list = new LinkedList<SubtitleEntry>();
                uuidsubs.put(e.getUuid(), list);
            }
            list.add(myEntry);

        }


        TreeMap<Long, List<SubtitleEntry>> sortedEntries = new TreeMap<Long, List<SubtitleEntry>>();

        for (LoggingRequest r : uuids) {
            String uuid = r.getUuid();
            List<SubtitleEntry> list = uuidsubs.get(uuid);
            if (list != null) {

                StringBuilder sb = new StringBuilder();
                int i = 1;
                for (SubtitleEntry e : list) {
                    long d1 = e.from - videoStart.getTimeInMillis() + delta;
                    long d2 = e.to - videoStart.getTimeInMillis() + delta;

                    sb.append(i++).append('\n').append(TimeFunctions.createSRTTimestamp(d1)).append(" --> ").append(TimeFunctions.createSRTTimestamp(d2)).append('\n').append(e.text).append("\n\n");
                    //total.append(totali++).append('\n').append(TimeFunctions.createSRTTimestamp(d1)).append(" --> ").append(TimeFunctions.createSRTTimestamp(d2)).append('\n').append(e.getUuid()).append(":").append(e.text).append("\n\n");
                    List<SubtitleEntry> mylist = sortedEntries.get(e.from);
                    if (mylist == null) {
                        mylist = new LinkedList<>();
                        sortedEntries.put(e.from, mylist);
                    }
                    mylist.add(e);
                }
                File f = new File(path + "/" + filename + "." + shortUUID(uuid) + ".srt");
                KosmosFileUtils.writeToFile(f, sb.toString());
            }
        }
        StringBuilder total = new StringBuilder();
        int totali = 1;
        for (List<SubtitleEntry> v : sortedEntries.values()) {
            for (SubtitleEntry e : v) {
                long d1 = e.from - videoStart.getTimeInMillis() + delta;
                long d2 = e.to - videoStart.getTimeInMillis() + delta;
                total.append(totali++).append('\n').append(TimeFunctions.createSRTTimestamp(d1)).append(" --> ").append(TimeFunctions.createSRTTimestamp(d2)).append('\n').append(shortUUID(e.getUuid())).append(":").append(e.text).append("\n\n");
            }
        }
        KosmosFileUtils.writeToFile(new File(path + "/" + filename + ".srt"), total.toString());
    }

    public boolean currentlyInTesting() {
        return runMode != RunMode.NORMAL;
    }

    public void delGroup(@Nonnull Group group) {
        logger.info("deleting group {}", group.getName());
        this.groups.remove(group.getName());
        this.groupsI.remove(group.getID());
        this.getPersistence().delGroup(group);
    }

    /**
     * delete an admin from a group
     *
     * @param group
     * @param user
     *
     * @return
     */
    public boolean delGroupAdmin(@Nonnull Group group, @Nonnull IUser user) {
        if (group.hasAdmin(user)) {
            this.getPersistence().delGroupAdmin(group, user);
            return true;
        }
        return false;
    }

    /**
     * delete a user from a group
     *
     * @param group
     * @param user
     *
     * @return
     */
    public boolean delGroupUser(@Nonnull Group group, IUser user) {
        if (group.hasUser(user)) {
            this.getPersistence().delGroupUser(group, user);
            return true;
        }
        return false;
    }

    /**
     * deletes an admin from a scope
     *
     * @param scope
     * @param user
     *
     * @return
     */
    public boolean delScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user) {
        if (scope.hasAdmin(user)) {
            this.getPersistence().delScopeAdmin(scope, user);
            return true;
        }
        return false;
    }

    /**
     * deletes a user from the scope
     *
     * @param scope
     * @param user
     *
     * @return
     */
    public boolean delScopeUser(@Nonnull Scope scope, @Nonnull IUser user) {
        if (scope.hasUser(user)) {
            this.getPersistence().delScopeUser(scope, user);
            return true;
        }
        return false;
    }

    /**
     * delete a device from the System, will also delete it from persistence
     *
     * @param from   which source wants to delete the device
     * @param device the device to be deleted
     * @param source the source
     */
    public void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {
        this.devices.remove(device.getUniqueID());
        for (CommandInterface t : this.commandInterfaces) {

            t.deviceRemoved(from, device, source);
        }
    }

    /**
     * delete a device from the system
     *
     * @param from
     * @param device
     */
    public void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device) {
        deleteDevice(from, device, getSource(from.getSourceName()));
    }

    public void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull String source) {
        deleteDevice(from, device, getSource(source));
    }

    @Override
    public void deleteSchema(@Nonnull DataSchema schema) {
        this.getPersistence().delSchema(schema);
        this.schemas.remove(schema.getSchema().getId());

    }

    @Override
    public void deleteScope(@Nonnull Scope scope) {

        this.persistence.delScope(scope);
        this.scopes.remove(scope.getName());
        this.scopesI.remove(scope.getID());
    }

    public void deleteUser(@Nonnull IUser user) {
        this.users.remove(user.getName().toLowerCase());
        this.getPersistence().delUser(user);
    }

    public DataSchema findSchema(@Nonnull String manufacturer, @Nonnull String model) throws SchemaNotFoundException {
        /*try {
            //TODO: need to add a repo for devices
            //DataSchema s = this.getSchema(fallBackSchema);

            return this.getSchema(fallBackSchema);
        } catch (NotFoundInPersistenceException ex) {

            try {
                //fall back to fallback
                return this.getSchema(fallBackSchema);
            } catch (NotFoundInPersistenceException e) {
                //return null
                throw new SchemaNotFoundException("");
            }
        }*/
        return this.getFallBackSchema();
    }

    public Collection<Device> getAllDevices() {
        HashSet<Device> set = new HashSet<>();
        set.addAll(this.devices.values());
        return set;
    }

    public Collection<Group> getAllGroupsWithAdmin(@Nonnull IUser user) {
        LinkedList<Group> list = new LinkedList<>();
        for (Group s : groups.values()) {
            if (s.hasAdmin(user)) {
                list.add(s);

            }
        }
        return list;
    }

    public Collection<Group> getAllGroupsWithUser(@Nonnull IUser user) {
        LinkedList<Group> list = new LinkedList<>();
        for (Group s : groups.values()) {
            if (s.hasUser(user)) {
                list.add(s);

            }
        }
        return list;
    }

    public Collection<DataSchema> getAllSchemas() {
        TreeSet<DataSchema> set = new TreeSet<>(new SchemaComparator());
        set.addAll(schemas.values());

        return set;
    }

    public Collection<Scope> getAllScopesWithAdmin(@Nonnull IUser user) {
        LinkedList<Scope> list = new LinkedList<>();
        for (Scope s : scopes.values()) {
            if (s.hasAdmin(user)) {
                list.add(s);

            }
        }
        return list;
    }

    public Collection<Scope> getAllScopesWithUser(@Nonnull IUser user) {
        LinkedList<Scope> list = new LinkedList<>();
        for (Scope s : scopes.values()) {
            if (s.hasUser(user)) {
                list.add(s);

            }
        }
        return list;
    }

    public Config getConfig() {
        return this.config;
    }

    @Nonnull
    public Device getDevice(@Nonnull String uuid) throws DeviceNotFoundException {
        Device d = this.devices.get(uuid);
        if (d != null) {
            return d;
        }
        throw new DeviceNotFoundException(uuid);
    }

    @Nonnull
    public DataSchema getFallBackSchema() {
        if (fallbackSchema == null) {
            try {
                fallbackSchema = new DataSchema(new JSONObject().put("$id", "https://kosmos-lab.de/schema/NoSchema.json").put("$schema", "http://json-schema.org/draft-07/schema#").put("additionalProperties", true).put("failures", new JSONArray()).put("examples", new JSONArray()));
            } catch (NotObjectSchemaException e) {
                throw new RuntimeException("could not convert the fallback schema to a DataSchema");
            }
        }
        return fallbackSchema;
    }

    public GestureProvider getGestureProvider() {

        return this.gestureProvider;
    }

    @Nonnull
    public Group getGroup(int id, CacheMode cacheMode) throws GroupNotFoundException {
        Group group = this.groupsI.get(id);
        if (cacheMode == CacheMode.CACHE_AND_PERSISTENCE && group == null) {
            try {
                group = this.getPersistence().getGroup(id);
            } catch (NotFoundInPersistenceException e) {

            }
        }
        if (group == null) {
            throw new GroupNotFoundException(id);
        }
        return group;

    }

    @Nonnull
    public Group getGroup(@Nonnull String name, CacheMode cacheMode) throws GroupNotFoundException {
        //logger.info("looking for group {}", name);
        Group group = this.groups.get(name);
        if (cacheMode == CacheMode.CACHE_AND_PERSISTENCE && group == null) {
            //logger.info("looking for group {} - NOT IN CACHE", name);
            try {
                return this.getPersistence().getGroup(name);
            } catch (NotFoundInPersistenceException ex) {

            }
        }

        if (group == null) {
            //logger.info("looking for group {} - NOT FOUND", name);
            throw new GroupNotFoundException(name);
        }
        //logger.info("looking for group {} - FOUND", name);
        return group;

    }

    /**
     * Gets ha device by domain.
     *
     * @param entityid the entityid
     *
     * @return the ha device by domain
     */
    public Device getHADeviceByDomain(String entityid) {
        try {
            try {
                return getDevice(entityid);
            } catch (DeviceNotFoundException exx) {
                Device d = null;
                Matcher m = Constants.domainSplitPattern.matcher(entityid);
                if (m.matches()) {
                    String uuid = m.group("uuid");
                    int lastIndex = 0;
                    while (lastIndex >= 0) {
                        try {
                            int nextIndex = uuid.indexOf('_', lastIndex);
                            if (nextIndex > -1) {
                                String uuid1 = uuid.substring(0, nextIndex);
                                String attr1 = uuid.substring(nextIndex + 1);
                                logger.warn("split domain {} {}", uuid1, attr1);
                                d = this.getDevice(uuid1);
                                try {
                                    if (d != null) {
                                        logger.warn("dev found for split domain {} {}", uuid1, attr1);
                                        if (d.has(attr1)) {
                                            logger.warn("FOUND! split domain {} {}", uuid1, attr1);
                                            return d;
                                        }

                                        for (String key : d.keySet()) {
                                            try {
                                                if (key.equalsIgnoreCase(attr1)) {
                                                    logger.warn("FOUND! split domain {} {}", uuid1, attr1);
                                                    return d;
                                                }
                                            } catch (Exception ex) {
                                                logger.warn("Exception:", ex);

                                            }
                                        }
                                        d = null;
                                    }

                                    lastIndex = nextIndex + 1;
                                    if (lastIndex >= uuid.length()) {
                                        return null;
                                    }
                                } catch (Exception ex) {
                                    logger.warn("Exception:", ex);
                                }
                            }

                        } catch (Exception ex) {
                            logger.warn("exception", ex);
                        }
                    }

                    //String[] toks = ;


                }
                return d;
            }

        } catch (Exception ex) {
            logger.error("error in HADevice", ex);

        }
        return null;

    }

    public JWT getJwt() {
        return this.jwt;
    }

    public @CheckForNull
    Device.Location getLocation(@Nonnull IUser user, @Nonnull String uuid) throws DeviceNotFoundException, NoAccessToScope {

        return this.getDevice(uuid, user, Scope.ScopeType.read).getLocation();


    }

    public List<LogEntry> getLogs(@Nonnull Calendar calStart, @Nonnull Calendar calEnd, @Nonnull String[] uuids) {
        return this.getPersistence().getStates(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), uuids);
    }

    public List<LogEntry> getLogs(@Nonnull Calendar calStart, @Nonnull Calendar calEnd, @Nonnull Set<String> uuids) {
        return this.getPersistence().getStates(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), uuids);
    }

    @Override
    public Collection<String> getMatchingUUID(@Nonnull String u) {
        LinkedList<String> list = new LinkedList<>();

        for (Device d : this.getAllDevices()) {
            if (Wildcard.matches(u, d.getUniqueID())) {
                list.add(d.getUniqueID());
            }
        }
        return list;
    }

    public String getPasswordHash(String input, String salt) {
        return HashFunctions.getSaltedAndPepperdHash(input, salt, getPepper());
    }

    /**
     * Gets pepper.
     *
     * @return the pepper
     */
    protected String getPepper() {
        return this.config.getString("pepper");
    }

    @Override
    public IPersistence getPersistence() {
        return this.persistence;
    }

    public RulesService getRulesService() {
        return this.rulesService;
    }

    public DataSchema getSchema(@Nonnull String id) throws SchemaNotFoundException {
        DataSchema s = this.schemas.get(id);
        if (s != null) {
            return s;
        }

        try {
            return this.getPersistence().getSchema(id);

        } catch (NotFoundInPersistenceException e) {

        }

        try {
            HTTPClient c = new HTTPClient();
            JSONObject o = c.fetchJSONObject(id, HttpMethod.GET);
            if (o != null) {
                s = new DataSchema(o);
                this.addSchema(s);
                return s;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }


        throw new SchemaNotFoundException(id);

    }

    public Scope getScope(int id, CacheMode cacheMode) throws NotFoundInPersistenceException {
        Scope scope = this.scopesI.get(id);
        if (cacheMode == CacheMode.CACHE_AND_PERSISTENCE && scope == null) {
            scope = this.getPersistence().getScope(id);
        }
        return scope;

    }

    @Nonnull
    public Scope getScope(@Nonnull String name, CacheMode cacheMode) throws NotFoundInPersistenceException {
        //logger.info("looking for scope {}", name);
        Scope scope = this.scopes.get(name);
        if (cacheMode == CacheMode.CACHE_AND_PERSISTENCE && scope == null) {
            //logger.info("looking for scope {} - NOT IN CACHE", name);
            scope = this.getPersistence().getScope(name);
            //logger.info("looking for scope {} - FOUND", name);
        }
        return scope;

    }

    @SuppressFBWarnings("AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTRACTION")
    public CommandSourceName getSource(@Nonnull String key) {
        CommandSourceName s = cacheSource.get(key);
        if (s == null) {
            s = () -> key;
            this.cacheSource.put(key, s);
        }
        return s;
    }

    public HashMap<Device, StateUpdates> getUpdates(@Nonnull Collection<String> uuids, long maxage) {
        HashMap<Device, StateUpdates> updates = new HashMap<>();
        for (String u : uuids) {

            try {
                Device d = this.getDevice(u);

                StateUpdates changes = d.getChanges(maxage);
                if (changes != null) {
                    updates.put(d, changes);
                }

            } catch (DeviceNotFoundException ex) {
                logger.info("Exception:", ex);
            }


        }
        return updates;

    }

    public IUser getUser(@Nonnull String username) {

        IUser user = users.get(username.toLowerCase());
        if (user == null) {
            try {
                user = this.getPersistence().getUser(username);
            } catch (NotFoundInPersistenceException e) {
                logger.warn("could not find user {}", username);

            }

        }
        return user;
    }

    /**
     * get a user from the system
     *
     * @param userid
     *
     * @return
     *
     * @throws UserNotFoundException will be thrown if there is no user with the ID
     */
    public IUser getUser(int userid) throws UserNotFoundException {
        IUser user = usersID.get(userid);
        if (user == null) {
            try {
                user = this.getPersistence().getUser(userid);
            } catch (NotFoundInPersistenceException e) {
                logger.warn("could not find user {}", userid);
                throw new UserNotFoundException("" + userid);
            }

        }
        return user;
    }

    public IUser getUserCreateIfUnavailable(@Nonnull String user) {
        logger.info("looking for {}",user);
        IUser u = this.getUser(user);
        if (u == null) {
            logger.info("needing to create user {}",user);
            this.addUser(user, StringFunctions.generateRandomKey(), 1);
            u = this.getUser(user);

        }
        logger.info("returning {}",u);
        return u;

    }

    public KosmoSWebServer getWebServer() {
        return webServer;
    }

    public String hashPepper(@Nonnull String input) {
        return HashFunctions.getSaltedHash(input, getPepper());

    }

    public String hashSaltPepper(@Nonnull String input, @Nonnull String salt) {
        return HashFunctions.getSaltedAndPepperdHash(input, salt, getPepper());
    }

    public void parseAddDevice(@Nonnull CommandInterface from, @Nonnull JSONObject o, @Nonnull CommandSourceName source, @Nonnull IUser user) throws ValidationException, DeviceAlreadyExistsException, ParameterNotFoundException, SchemaNotFoundException {
        parseAddDevice(from, o, source, user, true);
    }

    public void parseAddDevice(@Nonnull CommandInterface from, @Nonnull JSONObject o, @Nonnull CommandSourceName source, @Nonnull IUser user, boolean permanent) throws ValidationException, DeviceAlreadyExistsException, ParameterNotFoundException, SchemaNotFoundException {
        if (o != null) {
            DataSchema schema;
            String schemaName = o.optString("schema", null);
            if (schemaName != null) {
                //schema = DataFactory.getSchema(schemaName);
                schema = getSchema(schemaName);
            } else {
                throw new ParameterNotFoundException("schema");
            }
            if (schema != null) {
                if (o.has("uuid")) {
                    String uuid = o.getString("uuid");
                    try {
                        getDevice(uuid);

                        throw new DeviceAlreadyExistsException(uuid);

                    } catch (DeviceNotFoundException ex) {
                        //expected
//                        logger.info("Exception:", ex);
                    }

                    String name = o.optString("name", uuid);

                    JSONObject json = o.optJSONObject("state");
                    if (json == null) {
                        json = new JSONObject();
                    }


                    Device d = new Device(this, source, schema, json, name, uuid, user, false);


                    addDevice(from, d, permanent, source);
                    try {
                        if (o.has("scopes")) {
                            JSONObject scopes = o.getJSONObject("scopes");
                            if (scopes.has("read")) {
                                Scope scope = null;
                                String sname = scopes.getString("read");
                                try {

                                    scope = this.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                                } catch (NotFoundInPersistenceException e) {
                                    //e.printStackTrace();
                                    try {
                                        scope = addScope(sname, user);
                                    } catch (ScopeAlreadyExistsException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                if (scope != null) {
                                    setReadScope(d, scope);
                                }
                            }
                            if (scopes.has("write")) {
                                Scope scope = null;
                                String sname = scopes.getString("write");
                                try {

                                    scope = this.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                                } catch (NotFoundInPersistenceException e) {
                                    //e.printStackTrace();
                                    try {
                                        scope = addScope(sname, user);
                                    } catch (ScopeAlreadyExistsException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                if (scope != null) {
                                    setWriteScope(d, scope);
                                }
                            }
                            if (scopes.has("del")) {
                                Scope scope = null;
                                String sname = scopes.getString("del");
                                try {

                                    scope = this.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                                } catch (NotFoundInPersistenceException e) {
                                    //e.printStackTrace();
                                    try {
                                        scope = addScope(sname, user);
                                    } catch (ScopeAlreadyExistsException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                if (scope != null) {
                                    setDelScope(d, scope);
                                }
                            }
                        }
                    } catch (JSONException ex) {

                    }
                    cacheDevice(d);
                    publishUpdate(from, d, null, source);

                    return;

                }
                throw new ParameterNotFoundException("uuid");
            } else {
                throw new SchemaNotFoundException(schemaName);
            }

        }
    }

    /**
     * parse set from the HomeAssistant Interface. * If the device does not exist, it will be created from the current
     * state * if the deivce does exist, we set the state accordingly, see @see parseSet
     *
     * @param from
     * @param uuid
     * @param json
     * @param source
     * @param user
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws ValidationException
     * @throws NoAccessToScope
     */
    public Device parseHASet(@Nonnull CommandInterface from, @Nonnull String uuid, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope {

        Device d = null;


        int ptIndex = uuid.indexOf('.');
        if (ptIndex > 0) {
            try {
                d = getDevice(uuid.substring(ptIndex + 1));
            } catch (DeviceNotFoundException ex) {

            }
        }

        try {
            d = getDevice(uuid);
        } catch (DeviceNotFoundException exx) {

        }
        if (d == null) {

            if (json.has("state")) {
                Object state = json.get("state");
                if (state instanceof JSONObject) {
                    logger.info("state is object {}", state);
                    JSONObject sjson = (JSONObject) state;
                    String device_class = sjson.optString("device_class");
                    if (device_class != null && device_class.equalsIgnoreCase("update")) {
                        return null;
                    }

                    if (sjson.has("supported_color_modes")) {
                        logger.info("has SCM {} {}", uuid, sjson.get("supported_color_modes"));
                        try {
                            JSONArray scm = sjson.getJSONArray("supported_color_modes");
                            if (JSONFunctions.hasEntries(scm, new String[]{"color_temp", "xy"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HAXYCCTLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"color_temp", "hs"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HAHSCCTLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"color_temp"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HACCTLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"rgbw"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HARGBWLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"rgb"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HARGBLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"brightness"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HADimLamp.json");
                            } else if (JSONFunctions.hasEntries(scm, new String[]{"onoff"})) {
                                json.put("schema", "https://kosmos-lab.de/schema/HAToggleLamp.json");
                            }
                        } catch (Exception ex) {

                        }
                    } else {
                        logger.info("has NO SCM {}", uuid);
                        if (uuid.startsWith("zone.") &&
                                JSONFunctions.hasEntries(sjson, new String[]{"latitude", "longitude", "radius", "passive", "state"})) {
                            json.put("schema", "https://kosmos-lab.de/schema/HAZone.json");
                        } else if (uuid.startsWith("person.") &&
                                JSONFunctions.hasEntries(sjson, new String[]{"user_id"})) {
                            json.put("schema", "https://kosmos-lab.de/schema/HAPerson.json");
                        } else if (uuid.startsWith("sun.") &&
                                JSONFunctions.hasEntries(sjson, new String[]{"next_rising"})) {
                            json.put("schema", "https://kosmos-lab.de/schema/HASun.json");
                        } else if (uuid.startsWith("weather.") &&
                                JSONFunctions.hasEntries(sjson, new String[]{"forecast"})) {
                            json.put("schema", "https://kosmos-lab.de/schema/HAWeather.json");
                        } else if (uuid.startsWith("switch.")) {

                            json.put("schema", "https://kosmos-lab.de/schema/HASwitch.json");

                        } else if (uuid.startsWith("cover.")) {

                            json.put("schema", "https://kosmos-lab.de/schema/HACover.json");

                        } else if (uuid.startsWith("sensor.")) {
                            String state_class = sjson.optString("state_class");
                            if (state_class != null) {
                                if (state_class.equalsIgnoreCase("humidity")) {

                                }
                            }
                            json.put("schema", "https://kosmos-lab.de/schema/HASensor.json");

                        } else if (uuid.startsWith("scene.")) {
                            return null;

                        }

                    }
                }
            }
            if (!json.has("schema")) {
                json.put("schema", this.getFallBackSchema().getSchema().getId());
            }

            try {
                try {
                    if (!json.has("uuid")) {
                        json.put("uuid", uuid);
                    }

                    this.parseAddDevice(from, json, source, user, false);
                    d = getDevice(uuid);
                    if (from instanceof KosmoSWebSocketService) {
                        ((KosmoSWebSocketService) from).addIgnoredDevice(user, d);
                    }
                    return d;
                } catch (ValidationException ex) {
                    logger.info("could NOT validate\nschema: {}\ndata:{}", json.getJSONObject("state"), json.getString("schema"));
                    throw ex;
                }
            } catch (DeviceAlreadyExistsException e) {
                e.printStackTrace();
            } catch (ParameterNotFoundException e) {
                e.printStackTrace();
            } catch (SchemaNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("parse new JSON for device {}:{}", d.getUniqueID(), json);

            if (json.has("state")) {
                Object state = json.get("state");
                if (state instanceof JSONObject) {
                    try {
                        JSONObject j = json.getJSONObject("state");
                        if (j.length() > 0) {
                            try {
                                parseSet(from, d, j, source, user);
                            } catch (ValidationException ex) {
                                //logger.error("trying to fix ValidationException {}",ex.getMessage());
                                JSONObject newJSON = fixValidation(j, ex);
                                while (true) {
                                    if (newJSON != null) {
                                        try {
                                            parseSet(from, d, newJSON, source, user);
                                            return d;
                                        } catch (ValidationException exx) {
                                            //logger.error("trying to fix ValidationException again {}",exx.getMessage());
                                            newJSON = fixValidation(newJSON, exx);

                                        }
                                    } else {
                                        throw ex;
                                    }
                                }
                            }


                            return d;
                        }
                        json.remove("state");
                    } catch (ValidationException ex) {
                        logger.info("could NOT validate: {} - {}", json.getJSONObject("state"), ex.toJSON());
                        throw ex;
                    }

                }

            }
            try {
                parseSet(from, uuid, json, source, user);
            } catch (ValidationException ex) {
                logger.info("could NOT validate\nschema: {}\ndata:{}", json.getJSONObject("state"), json.getString("schema"));
                throw ex;
            }
        }


        return d;
    }

    /**
     * parse a set command
     *
     * @param from   the CommandInterface from which this command came
     * @param uuid
     * @param json
     * @param source
     * @param user
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws ValidationException
     * @throws NoAccessToScope
     */
    public Device parseSet(@Nonnull CommandInterface from, @Nonnull String uuid, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope {
        Device device = getDevice(uuid, user, Scope.ScopeType.write);

        return parseSet(from, device, json, source, user);
    }

    /**
     * parse a set command
     *
     * @param from   the CommandInterface from which this command came
     * @param device
     * @param json
     * @param source
     * @param user
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws ValidationException
     * @throws NoAccessToScope
     */
    public Device parseSet(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope {
        json.remove("uuid");
        json.remove("id");
        try {
            device.lock();
            if (device.canWrite(user)) { //throws NoAccessToScope
                HashSet<String> newKeys = new HashSet<>();
                for (String key : json.keySet()) {
                    if (!device.has(key)) {
                        newKeys.add(key);
                    }
                }
                if (!newKeys.isEmpty()) {
                    //PropertyTranslator.transform(device, json, newKeys);
                }


                if (!(from instanceof IPersistence)) {

                    //force fix broken keys for devices which cannot return a real error
                    Iterator<String> iter = json.keySet().iterator();
                    HashSet<String> toClean = new HashSet<>();
                    while (iter.hasNext()) {

                        String k2 = iter.next();
                        if (from != webServer) {
                            if (!device.canHave(k2)) {
                                toClean.add(k2);
                            }

                            if (!device.canWrite(k2, user)) {
                                toClean.add(k2);
                                ;
                            }

                        } else {
                            if (!device.canWrite(k2, user)) {
                                throw new ValidationException("cannot change value of " + k2);
                            }
                        }


                    }
                    for (String key : toClean) {
                        json.remove(key);
                    }
                }
                device.updateFromJSON(from, json, source);
                return device;
            }
        } finally {
            device.unlock();

        }
        return null;
    }

    public void publishUpdate(@CheckForNull CommandInterface from, @Nonnull Device device, String key, @Nonnull CommandSourceName source) {


        logger.info("pub update {} {} {} {}:{}", device.getUniqueID(), from, source.getSourceName(), key, ((key !=
                null) ? (device.opt(key)) : ("")));
        for (CommandInterface t : commandInterfaces) {
            //logger.info("send update to {}", t.getSourceName());
            //logger.info(t.getSourceName());
            (new Thread(() -> t.deviceUpdate(from, device, key, source))).start();
        }

    }

    public void setDelScope(@Nonnull Device device, @Nonnull Scope scope) {
        device.setDelScope(scope);
        this.getPersistence().setDelScope(device, scope);
    }

    public Device.Location setLocation(@Nonnull IUser user, @Nonnull JSONObject json, @Nonnull CommandSourceName source) throws DeviceNotFoundException, NoAccessToScope, ParameterNotFoundException {
        if (!json.has("uuid")) {
            throw new ParameterNotFoundException("uuid");
        }
        String uuid = json.getString("uuid");
        Device d = this.getDevice(uuid, user, Scope.ScopeType.write);


        Device.Location loc = d.getLocation();
        if (loc == null) {
            loc = new Device.Location(json);
            d.setLocation(loc);
            this.getPersistence().updateLocation(d);

        } else {
            if (loc.updateFromJSON(json)) {
                this.getPersistence().updateLocation(d);
            }
        }
        if (this.webServer != null) {
            KosmoSWebSocketService wss = webServer.getWebSocketService();
            if (wss != null) {
                wss.broadcastToReadUsers(d,
                        "device/" + d.getUniqueID() + "/location:" + loc.toJSON(), source);
            }
        }
        return loc;

    }

    public void setName(@Nonnull Device device, @Nonnull String name) {
        this.getPersistence().setName(device, name);
    }

    public void setReadScope(@Nonnull Device device, @Nonnull Scope scope) {
        device.setReadScope(scope);
        this.getPersistence().setReadScope(device, scope);
    }

    @Override
    public void setUserPassword(@Nonnull IUser user, @Nonnull String salt, @Nonnull String hash) {

        this.getPersistence().setPassword(user, salt, hash);

    }

    /**
     * sets the write scope for a device
     *
     * @param device
     * @param scope
     */
    public void setWriteScope(@Nonnull Device device, @Nonnull Scope scope) {
        device.setWriteScope(scope);
        this.getPersistence().setWriteScope(device, scope);
    }

    private void setupHA() {

        if (KosmoSHelper.getEnvBool("DONTSETUPHA")) {
            return;
        }

        try {

            if (KosmoSHelper.getEnvBool("SETUPHA")) {
                String ha_host = KosmoSHelper.getEnv("HA_HOST");
                if (ha_host == null) {
                    ha_host = "kha";
                }
                String kosmos_host = KosmoSHelper.getEnv("KOSMOS_HOST");
                if (kosmos_host == null) {
                    kosmos_host = "kcontroller";
                }
                setupHA("http://" + ha_host + ":8123", "http://" + kosmos_host + ":" + this.webServer.getPort());
            } /*else {
                HashMap<String, String> ips = new HashMap<String, String>();
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    // drop inactive
                    if (!networkInterface.isUp())
                        continue;

                    // smth we can explore
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        InetAddressValidator validator = InetAddressValidator.getInstance();
                        if (validator.isValidInet4Address(addr.getHostAddress())) {
                            logger.info("NetInterface: name {}, ip {}",
                                    networkInterface.getDisplayName(), addr.getHostAddress());
                            ips.put(networkInterface.getDisplayName(), addr.getHostAddress());
                        }
                    }
                }
                for (String key : new String[]{"wlan0", "eth0"}) {
                    if (ips.containsKey(key)) {
                        if (setupHA("http://localhost:8123", "http://" + ips.get(key) + ":" + this.webServer.getPort())) {
                            return;
                        }
                    }
                }
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getFileString(String name) {
        return getFileString(name, this.runMode);
    }

    public File getFile(String name) {
        return getFile(name, this.runMode);
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private boolean setupHA(@Nonnull String habase, @Nonnull String myhost) {
        try {
            String ha_user = KosmoSHelper.getEnv("HA_USER");
            if (ha_user == null) {
                ha_user = "kosmos";
            }
            String ha_pass = KosmoSHelper.getEnv("HA_PASS");
            if (ha_pass == null) {
                ha_pass = "pass";
            }
            int tries_left = 20;
            while (tries_left > 0) {
                try {
                    logger.info("trying to connect to: {}", habase);
                    URL url = new URL(habase);

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                    in.close();

                    logger.info("HA is reachable as it seems");

                    break;
                } catch (MalformedURLException e) {
                    return false;
                } catch (IOException e) {
                    if (tries_left-- > 0) {
                        Thread.sleep(10000);
                    }

                }
            }
            IUser u = getUser("homeassistant");
            File f = getFile("config/ha_password");

            String password = StringFunctions.generateRandomKey();
            if (u == null) {
                addUser("homeassistant", password, 1);
                logger.info("created new ha user with {}", password);
            } else {
                String USERS = KosmoSHelper.getEnv("USERS");
                if (USERS != null) {
                    try {
                        JSONArray arr = new JSONArray(USERS);
                        for (int i = 0; i < arr.length(); i++) {

                            JSONObject obj = arr.getJSONObject(i);
                            if (obj.getString("username").equals("ha")) {
                                password = obj.getString("password");
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                if (f.exists()) {
                    try {
                        password = de.kosmos_lab.utils.FileUtils.readFile(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    String salt = StringFunctions.generateRandomKey();
                    logger.info("updating ha pw to: {}", password);
                    setUserPassword(u, salt, getPasswordHash(password, salt));
                }
            }
            de.kosmos_lab.utils.FileUtils.writeToFile(f, password);
            final String pw = password;
            HomeAssistantHTTPClient haclient = new HomeAssistantHTTPClient(habase, ha_user, ha_pass);
            JSONObject obj = new JSONObject().put("client_id", haclient.getBase()).put("name", "kosmos").put("username", haclient.getUser()).put("password", haclient.getPass()).put("language", "en");

            JSONObject result = haclient.postJSON("/api/onboarding/users", obj);
            if (result.has("auth_code")) {
                HashMap<String, Object> p = new HashMap<String, Object>();
                p.put("client_id", haclient.getBase());
                p.put("code", result.getString("auth_code"));
                p.put("grant_type", "authorization_code");
                result = new JSONObject(haclient.post("/auth/token", p));
                haclient.setToken(result.getString("access_token"));
                haclient.connect();


                var ref = new Object() {
                    ScheduledFuture<?> t = null;
                };
                ref.t = scheduledExecutorService.scheduleAtFixedRate(() -> {
                    if (haclient.getWebSocket() != null && haclient.getWebSocket().isAuthed()) {

                        ref.t.cancel(false);
                    }


                }, 1000, 100, TimeUnit.MILLISECONDS);
                try {
                    ref.t.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (CancellationException e) {
                    // e.printStackTrace();
                }

                //{"type":"config/core/update","location_name":"BAALL","latitude":53.10595429039234,"longitude":8.854916095733644,"elevation":0,"unit_system":"metric","time_zone":"UTC","id":24}
                haclient.sendCommand(new JSONObject().put("type", "config/core/update").put("location_name", "BAALL").put("latitude", 53.105944629013486d).put("longitude", 8.85490268468857d).put("elevation", 0).put("unit_system", "metric").put("time_zone", "Europe/Berlin"), (client, json) -> {


                });

                haclient.sendCommand(new JSONObject().put("type", "analytics/preferences").put("preferences", new JSONObject()), (client, json) -> {


                });
                haclient.sendCommand(new JSONObject().put("type", "subscribe_events").put("event_type", "state_changed"), (client, json) -> {


                });

                scheduledExecutorService.schedule(() -> {
                            JSONObject jsonResult = haclient.postJSON("/api/config/config_entries/flow", new JSONObject().put("handler", "kosmos").put("show_advanced_options", true));

                            String flow_id = jsonResult.getString("flow_id");

                            haclient.postJSON("/api/config/config_entries/flow/" + flow_id, new JSONObject().put("host", myhost).put("username", "homeassistant").put("password", pw));

                        },
                        10,
                        TimeUnit.SECONDS).get();

                if (KosmoSHelper.getEnvBool("SETUPKNX")) {
                    scheduledExecutorService.schedule(() -> {
                                JSONObject jsonResult = haclient.postJSON("/api/config/config_entries/flow", new JSONObject().put("handler", "knx").put("show_advanced_options", false));
                                String flow_id = jsonResult.getString("flow_id");
                                haclient.postJSON("/api/config/config_entries/flow/" + flow_id, new JSONObject().put("connection_type", "automatic"));
                            },
                            10,
                            TimeUnit.SECONDS).get();
                }

                haclient.stop();
                return true;
            }

        } catch (Exception e) {

        }

        return false;
    }

    /**
     * checks if a device shall be logged, or not
     *
     * @param device
     *
     * @return
     */
    @Override
    public boolean shouldDeviceBeLogged(@Nonnull Device device) {
        Boolean v = logByDevice.get(device);
        if (v != null) {
            return v;
        }

        for (String filter : logBlackListFilter) {
            if (Wildcard.matches(filter, device.getUniqueID())) {
                return false;
            }
        }
        for (String filter : logWhiteListFilter) {
            if (Wildcard.matches(filter, device.getUniqueID())) {
                return true;
            }
        }
        return true;
    }

    /**
     * shut down the server
     */
    public void stop() {

        for (CommandInterface t : commandInterfaces) {
            t.stop();
        }
        this.stopped = true;
    }

    /**
     * try to login with the given credentials
     *
     * @param user
     * @param pass
     *
     * @return a user if the login was successful
     */
    public @CheckForNull
    IUser tryLogin(@CheckForNull String user, @CheckForNull String pass) throws de.kosmos_lab.web.exceptions.LoginFailedException {
        if (user == null || pass == null) {
            return null;
        }
        IUser u = getUser(user);

        if (u != null) {
            if (u.checkPassword(pass)) {
                return u;
            } else {
                logger.warn("user {} password mismatch!", user);
            }
        } else {
            logger.warn("user {} not found!", user);
        }
        for (IAuthProvider provider : this.authProviders) {
            try {
                 u = provider.tryLogin(user, pass);
                if (u != null) {
                    return u;
                }
            } catch (LoginFailedException ex) {
                throw ex;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (user.contains(":")) {
            return null;
        }
        
        return null;
    }

    /**
     * @param from     the interface which first received this command
     * @param source   the source of the command
     * @param device   the device to change
     * @param property the property we want to change
     * @param value    the new value
     *
     * @throws ValidationException
     */
    public void updateFromSource(@Nonnull CommandInterface from, @Nonnull CommandSourceName source, @Nonnull Device device, @Nonnull String property, Object value) throws ValidationException {

        if (device.set(property, value, true)) {
            this.publishUpdate(from, device, property, source);
        }
    }

    /**
     * update the device
     *
     * @param from     the interface which first received this command
     * @param source   the source of the command
     * @param uuid     the uuid of the device being controlled
     * @param property the property we want to change
     * @param value    the new value
     *
     * @throws ValidationException
     * @throws DeviceNotFoundException
     */
    public void updateFromSource(@Nonnull CommandInterface from, @Nonnull CommandSourceName source, @Nonnull String uuid, @Nonnull String property, @Nonnull Object value) throws ValidationException, DeviceNotFoundException {
        Device d = this.getDevice(uuid);
        if (d == null) {
            throw new DeviceNotFoundException(uuid);
        }
        updateFromSource(from, source, d, property, value);

    }

    /**
     * update the last Update timestamp of a device
     *
     * @param device the device to update
     */
    public void updateLastUpdate(@Nonnull Device device) {

        this.getPersistence().updateLastUpdate(device);

    }

    @Nonnull
    public Device getDevice(@Nonnull String uuid, @Nonnull IUser user) throws DeviceNotFoundException, NoAccessToScope {
        return getDevice(uuid, user, Scope.ScopeType.read);
    }

    @Nonnull
    public KosmosPluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Nonnull
    @Override
    public Collection<ICamera> getAllCameras() {
        return this.cameras.values();
    }

    @Nonnull
    @Override
    public ICamera getCamera(@Nonnull String name) throws CameraNotFoundException {
        ICamera cam = this.cameras.get(name);
        if (cam != null) {
            return cam;
        }
        throw new CameraNotFoundException(name);
    }

    @Override
    public void startRecording(@Nonnull ICamera cam, @Nonnull IUser user) {
        if (!cam.isRecording()) {
            File f = this.getRecordingFile(cam, user);
            cam.startRecording(f);
            this.recordings.put(cam, user);
        }
    }

    @SuppressFBWarnings("DM_EXIT")
    @Nonnull
    private File getRecordingFile(@Nonnull ICamera cam, @Nonnull IUser user) {
        File f = getRecordingDir(user);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                logger.warn("could not create recording folder  \"{}\" - exiting", f);
                System.exit(1);
            }
        }
        return new File(StringFunctions.format(Constants.recordingFile, Map.of("user", user.getUUID(), "cam", cam.getName(), "date", new SimpleDateFormat(Constants.dateFilePattern).format(new Date()))));

    }

    @Override
    public void stopRecording(@Nonnull ICamera cam, @Nonnull IUser user) throws NoAccessException {
        IUser recordingUser = this.recordings.get(cam);
        if (recordingUser != null) {
            if (user.isAdmin() || user.equals(recordingUser)) {
                cam.stopRecording();
            } else {
                throw new NoAccessException("you have no access here!");
            }
        } else {
            throw new NoAccessException("there is no recording running?!");
        }
    }

    @Nonnull
    private String getRecordingDirString(@Nonnull IUser user) {
        return StringFunctions.format(Constants.recordingDir, new Object[][]{{"user", user.getUUID()}});
    }

    @Nonnull
    private File getRecordingDir(@Nonnull IUser user) {
        return new File(getRecordingDirString(user));
    }

    @Override
    @Nonnull
    public Collection<File> listRecordings(@Nonnull ICamera cam, @Nonnull IUser user) {
        Collection<File> fileList = new LinkedList<File>();
        File dir = getRecordingDir(user);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(cam.getName() + "_")) {
                        fileList.add(f);
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public byte[] getRecording(@Nonnull IUser user, @Nonnull String filename) throws NoAccessToRecording {
        File f = new File(getRecordingDirString(user) + "/" + filename);
        if (f.exists()) {
            try {
                return de.kosmos_lab.utils.FileUtils.readBinary(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new NoAccessToRecording();

    }

    @Nonnull
    public Device getDevice(@Nonnull String uuid, @Nonnull IUser user, @Nonnull Scope.ScopeType scopeType) throws DeviceNotFoundException, NoAccessToScope {
        Device device = getDevice(uuid);
        switch (scopeType) {
            case read:
                if (device.canRead(user)) {
                    return device;
                }
            case write:
                if (device.canWrite(user)) {
                    return device;
                }
            case delete:
                if (device.canDel(user)) {
                    return device;
                }

        }
        throw new DeviceNotFoundException(uuid);
    }

    public MQTTBroker getMQTT() {
        return this.mqttBroker;
    }

    public boolean isStopped() {
        return this.stopped;
    }
}
