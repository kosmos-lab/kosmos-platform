package de.kosmos_lab.kosmos.data;

import de.dfki.baall.helper.webserver.data.IUser;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.smarthome.CommandInterface;
import de.kosmos_lab.kosmos.platform.smarthome.CommandSourceName;
import de.kosmos_lab.utils.DoubleFunctions;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used as an actual Device.
 * It holds the current states, scopes and so on.
 */
public class Device extends DataEntry {
    protected static final Logger logger = LoggerFactory.getLogger("Device");
    private final IController controller;
    private final String uuid;
    private final IUser owner;
    protected HashMap<String, Object> hidden = new HashMap<>();
    ConcurrentHashMap<String, Long> lastChangeMap = new ConcurrentHashMap<>();
    private String name;
    private CommandSourceName source;
    private Date lastUpdate;
    private Date lastChange;
    private Scope delScope;
    private Scope readScope;
    private Scope writeScope;
    private Location location = null;
    
    public Device(@Nonnull IController controller,@Nonnull  CommandSourceName source,@Nonnull  DataSchema schema,@Nonnull  JSONObject json, @Nonnull String name, @Nonnull String uuid, @Nonnull IUser owner,boolean force) throws ValidationException {
        super(schema, json,force);
        this.controller = controller;
        this.source = source;
        this.name = name;
        this.uuid = uuid;
        Date now = new Date();
        
        this.lastUpdate = now;
        this.lastChange = now;
        this.owner = owner;
    }
    
    /**
     * @param user
     * @return
     * @throws NoAccessToScope
     */
    public boolean canDel(@Nonnull IUser user) throws NoAccessToScope {
        if (delScope == null) {
            return true;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (delScope.hasAccess(user)) {
            return true;
        }
        
        throw new NoAccessToScope(delScope);
    }
    
    public boolean canHave(@Nonnull String key, boolean def) {
        if (has(key)) {
            return true;
        }
        if (getDataSchema() != null) {
            return getDataSchema().definesProperty(key);
        }
        return def;
    }
    
    public boolean canHave(@Nonnull String key) {
        
        
        return canHave(key, true);
        
    }
    
    public boolean canRead(@Nonnull IUser user) throws NoAccessToScope {
        //logger.info("CAN READ? {} {}",readScope,user.getID());
        if (readScope == null) {
            return true;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (readScope.hasAccess(user)) {
            return true;
        }
        
        throw new NoAccessToScope(readScope);
    }
    
    public boolean canWrite(@Nonnull IUser user) throws NoAccessToScope {
        if (writeScope == null) {
            return true;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (writeScope.hasAccess(user)) {
            return true;
        }
        
        throw new NoAccessToScope(writeScope);
    }
    
    public boolean canWrite(@Nonnull String key,@Nonnull  IUser user) {

        if (owner == null) {
            logger.error("Device {} has NO OWNER?!", this.getUniqueID());
            return true;
        }
        try {
            Schema schema = getSchema().getPropertySchemas().get(key);
            if (schema != null) {
                if (schema.isReadOnly() == null) {
                    return true;
                }
                if (schema.isReadOnly()
                        
                        && !canWriteReadOnly(user)) {
                    return false;
                }
                
                
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            logger.warn("could not verify readonly state of {} in {}", key, this.getSchema().getId(), ex);
        }
        return true;
        
        
    }
    
    public boolean canWriteReadOnly(@Nonnull IUser u) {
        
        if (owner.equals(u)) {
            return true;
        }
        return u.isAdmin();

    }
    
    @CheckForNull
    public StateUpdates getChanges(long maxage) {
        if (this.length() > 0) {
            //JSONObject changed = new JSONObject();
            StateUpdates upd = new StateUpdates();
            for (Map.Entry<String, Long> entry : lastChangeMap.entrySet()) {
                long delta = System.currentTimeMillis() - entry.getValue();
                if (delta <= maxage) {
                    //changed.put(entry.getKey(), this.get(entry.getKey()));
                    //updates.add(new StateUpdated(this,entry.getKey(),delta));
                    upd.addUpdate(entry.getKey(), delta);
                }
                
            }
            
            return upd;
            
            
        }
        return null;
        
    }

    @CheckForNull
    public Object getHidden(@Nonnull String key) {
        return hidden.get(key);
    }
    
    public Date getLastUpdated() {
        return this.lastUpdate;
    }

    @CheckForNull
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(@Nullable Location location) {
        this.location = location;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(@Nonnull String name) {
        this.name = name;
    }
    
    public IUser getOwner() {
        return this.owner;
    }
    
    public CommandSourceName getSource() {
        return source;
    }
    
    public void setSource(@Nonnull CommandSourceName source) {
        this.source = source;
    }
    
    @Nonnull public String getUniqueID() {
        return uuid;
    }
    
    @CheckForNull public String getUnit(@Nonnull String property) {
        JSONObject properties = this.getDataSchema().getRawSchema().optJSONObject("properties");
        if (properties != null) {
            JSONObject p = properties.optJSONObject(property);
            if (p != null) {
                return p.optString("unit", null);
            }
        }
        
        return null;
    }
    
    public boolean hasDelScope() {
        return this.delScope != null;
    }
    
    public boolean set(String key, Object value, boolean round) throws ValidationException {
        Date now = new Date();
        
        this.lastUpdate = now;
        if (controller != null) {
            controller.updateLastUpdate(this);
        }
        //logger.info("key {} type {} value {}", key, value.getClass(), value);
        //if its a string, check if it should not be..
        if (value instanceof Integer || value instanceof Double || value instanceof Float) {
            //logger.info("is a number");
            Schema schema = getSchema().getPropertySchemas().get(key);
            if (schema != null) {
                //logger.info("schematype {}", schema.getClass());
                if (schema instanceof NumberSchema) {
                    try {
                        //check if it should be a number
                        NumberSchema ns = ((NumberSchema) schema);
                        if (ns.requiresInteger()) {
                            //logger.info("value should be an int2");
                            //is it also an integer?
                            
                            Number multiplier = ns.getMultipleOf();
                            if (multiplier != null) {
                                value = DoubleFunctions.roundToNearest(((Number) value).doubleValue(), multiplier.intValue()).intValue();
                                
                            } else {
                                value = ((Number) value).intValue();
                            }
                            
                            
                        } else {
                            //logger.info("value should be a double");
                            //its "just" a number, so its a double
                            if ((value instanceof Integer)) {
                                value = ((Number) value).doubleValue();
                            }
                            Number multiplier = ns.getMultipleOf();
                            if (multiplier != null) {
                                value = DoubleFunctions.roundToNearest(((Number) value).doubleValue(), multiplier.doubleValue());
                                
                            }
                        }
                    } catch (NumberFormatException ex) {
                        //this means the number could not be read -but we can just ignore this here
                        //this essentially just means that we could not round the value
                        //let the validation throw the error, so its consistent
                    }
                } else if (schema instanceof EnumSchema) {
                    //logger.info("should be enum");
                    EnumSchema es = ((EnumSchema) schema);
                    boolean found = false;
                    List<Object> list = es.getPossibleValuesAsList();
                    if (!list.isEmpty()) {
                        for (Object f : list) {
                            if ((value.toString()).equalsIgnoreCase(f.toString())) {
                                found = true;
                                //logger.info("found matching enum {}", f);
                                value = f.toString();
                                
                                break;
                            }
                        }
                        if (!found) {
                            value = list.get(0).toString();
                        }
                    }
                } else if (schema instanceof StringSchema) {
                    //logger.info("should be string");
                    
                    try {
                        value = value.toString();
                        
                    } catch (NumberFormatException ex) {
                        //this means the number could not be read -but we can just ignore this here
                        //this essentially just means that we could not round the value
                        //let the validation throw the error, so its consistent
                    }
                } else if (schema instanceof CombinedSchema) {
                    //logger.info("should be CombinedSchema");
                    CombinedSchema cs = (CombinedSchema) schema;
                    
                    schemaloop:
                    for (Schema sub : cs.getSubschemas()) {
                        if (sub instanceof EnumSchema) {
                            //logger.info("should be enum");
                            EnumSchema es = ((EnumSchema) sub);
                            boolean found = false;
                            List<Object> list = es.getPossibleValuesAsList();
                            if (!list.isEmpty()) {
                                for (Object f : list) {
                                    if ((value.toString()).equalsIgnoreCase(f.toString())) {
                                        found = true;
                                        //logger.info("found matching enum {}", f);
                                        value = f.toString();
                                        
                                        break schemaloop;
                                    }
                                }
                                if (!found) {
                                    value = list.get(0).toString();
                                }
                            }
                        } else if (schema instanceof StringSchema) {
                            //logger.info("should be string");
                            value = value.toString();
                            
                            
                        }
                    }
                }
            }
        }
        if (value instanceof String) {
            //logger.info("value is a string");
            
            
            Schema schema = getSchema().getPropertySchemas().get(key);
            
            if (schema instanceof BooleanSchema) {
                //check if the schema of the property should be boolean
                if ("true".equalsIgnoreCase((String) value) || "on".equalsIgnoreCase((String) value) || value.equals("1") || value.equals("heat")) {
                    value = true;
                } else if ("false".equalsIgnoreCase((String) value) || "off".equalsIgnoreCase((String) value) || value.equals("0")) {
                    value = false;
                }
            } else if (schema instanceof NumberSchema) {
                
                
                try {
                    //check if it should be a number
                    NumberSchema ns = ((NumberSchema) schema);
                    if (ns.requiresInteger()) {
                        //logger.info("value should be an int");
                        //is it also an integer?
                        value = (Double.valueOf((String) value));
                        Number multiplier = ns.getMultipleOf();
                        if (multiplier != null) {
                            value = DoubleFunctions.roundToNearest((Double) value, multiplier.intValue()).intValue();
                            
                        } else {
                            value = ((Number) value).intValue();
                        }
                    } else {
                        //logger.info("value should be a double");
                        //its "just" a number, so its a double
                        value = Double.valueOf((String) value);
                        Number multiplier = ns.getMultipleOf();
                        if (multiplier != null) {
                            value = DoubleFunctions.roundToNearest((Double) value, multiplier.doubleValue());
                            
                        }
                    }
                } catch (NumberFormatException ex) {
                    //this means the number could not be read -but we can just ignore this here
                    //this essentially just means that we could not round the value
                    //let the validation throw the error, so its consistent
                }
            } else if (schema instanceof EnumSchema) {
                EnumSchema es = ((EnumSchema) schema);
                boolean found = false;
                List<Object> list = es.getPossibleValuesAsList();
                if (!list.isEmpty()) {
                    for (Object f : list) {
                        if (((String) value).equalsIgnoreCase(f.toString())) {
                            found = true;
                            value = f.toString();
                            
                            break;
                        }
                    }
                    if (!found) {
                        value = list.get(0).toString();
                    }
                }
            } else if (schema instanceof CombinedSchema) {
                //logger.info("should be CombinedSchema");
                CombinedSchema cs = (CombinedSchema) schema;
                
                schemaloop:
                for (Schema sub : cs.getSubschemas()) {
                    if (sub instanceof EnumSchema) {
                        //logger.info("should be enum");
                        EnumSchema es = ((EnumSchema) sub);
                        
                        List<Object> list = es.getPossibleValuesAsList();
                        if (!list.isEmpty()) {
                            for (Object f : list) {
                                if ((value.toString()).equalsIgnoreCase(f.toString())) {
                                    //logger.info("found matching enum {}", f);
                                    value = f.toString();
                                    
                                    break schemaloop;
                                }
                            }
                            
                        }
                        
                        value = list.get(0).toString();
                        
                    } else if (schema instanceof StringSchema) {
                        //logger.info("should be string");
                        value = value.toString();
                        
                        
                    }
                }
            } else if (schema instanceof ArraySchema) {
                //logger.info("should be CombinedSchema");
                
                //logger.info("should be string");
                value = new JSONArray(value);
                
                
                //}
                
            } else if (schema instanceof ObjectSchema) {
                //logger.info("should be CombinedSchema");
                
                //logger.info("should be string");
                value = new JSONObject(value);
                
                
                //}
                
            }
        }
        
        try {
            Object old = this.get(key);
            
            
            if (old instanceof JSONObject && value instanceof JSONObject) {
                //check if the old and new instance are both jsonobjects
                if (((JSONObject) old).toMap().equals(((JSONObject) value).toMap())) {
                    
                    return false;
                }
                if (old.toString().equals(value.toString())) {
                    //compare them
                    return false;
                }
                //{"name":"test","value":1243} == {"name":"test","value":1243}
                //{"name":"test","value":1243} != {"value":1243,"name":"test"}
            }
            if (old instanceof JSONArray && value instanceof JSONArray) {
                if (old.toString().equals(value.toString())) {
                    return false;
                }
                //[124,122,1] == [124,122,1]
            } else {
                //check if old and new is same, if yes - do nothing
                if (old.equals(value)) {
                    return false;
                }
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        //copy the current data
        JSONObject newState = new JSONObject(this.toMap());
        
        //set the new state
        newState.put(key, value);
        //logger.info(newState.toString(4));
        
        //validate the new state
        logger.info("state to validate {}", newState);
        this.getSchema().validate(newState);
        
        //can only be reached if value is valid for schema, so we can also modifiy the real version now
        put(key, value);
        this.lastChangeMap.put(key, System.currentTimeMillis());
        this.lastChange = now;
        //notify that we actually did have a change
        return true;
        
        
    }
    
    public void setDelScope(Scope scope) {
        this.delScope = scope;
    }
    
    public void setHidden(String key, Object value) {
        hidden.put(key, value);
    }
    
    public void setReadScope(Scope scope) {
        this.readScope = scope;
    }
    
    public void setState(JSONObject o, boolean b) throws ValidationException {
        Date now = new Date();
        this.getSchema().validate(o);
        silentOverwrite(o);
        this.lastUpdate = now;
        this.lastChange = now;
        
        
    }
    
    public void setWriteScope(Scope scope) {
        this.writeScope = scope;
    }
    
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        JSONObject state = new JSONObject();
        //o.put("schema",this.getSchema().toString());
        for (String key : this.keySet()) {
            state.put(key, this.get(key));
        }
        o.put("state", state);
        o.put("name", name);
        o.put("uuid", uuid);
        o.put("schema", this.getSchema().getId());
        o.put("lastUpdate", lastUpdate.getTime());
        o.put("lastChange", lastChange.getTime());
        return o;
    }
    
    public boolean updateFromJSON(CommandInterface from, JSONObject o, CommandSourceName source) {
        HashSet<String> dirtyKeys = new HashSet<>();
        for (String key : o.keySet()) {
            if (set(key, o.get(key), true)) {
                dirtyKeys.add(key);
            }
        }
        for (String key : dirtyKeys) {
            controller.publishUpdate(from, this, key, source);
        }
        return dirtyKeys.size() > 0;
    }
    
    public static class Location {
        private Integer x, y, z, w, d, h, roll, pitch, yaw;
        private String area;
        
        public Location(Integer x, Integer y, Integer z, Integer w, Integer d, Integer h, Integer roll, Integer pitch, Integer yaw, String area) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.d = d;
            this.h = h;
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;
            this.area = area;
        }
        
        public Location(JSONObject json) {
            if (json.has("x")) {
                try {
                    this.x = json.getInt("x");
                } catch (JSONException ex) {
                    this.x = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("y")) {
                try {
                    this.y = json.getInt("y");
                } catch (JSONException ex) {
                    this.y = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("z")) {
                try {
                    this.z = json.getInt("z");
                } catch (JSONException ex) {
                    this.z = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("w")) {
                try {
                    this.w = json.getInt("w");
                } catch (JSONException ex) {
                    this.w = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("h")) {
                try {
                    this.h = json.getInt("h");
                } catch (JSONException ex) {
                    this.h = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("d")) {
                try {
                    this.d = json.getInt("d");
                } catch (JSONException ex) {
                    this.d = null;
                    ex.printStackTrace();
                    
                }
            }
            if (json.has("roll")) {
                try {
                    this.roll = json.getInt("roll");
                } catch (JSONException ex) {
                    this.roll = null;
                    ex.printStackTrace();
                }
            }
            if (json.has("pitch")) {
                try {
                    this.pitch = json.getInt("pitch");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            if (json.has("yaw")) {
                try {
                    this.yaw = json.getInt("yaw");
                } catch (JSONException ex) {
                    this.yaw = null;
                    ex.printStackTrace();
                }
            }
            
            this.area = json.optString("area", null);
            
            
        }
        
        public String getArea() {
            return area;
        }
        
        public Integer getD() {
            return d;
        }
        
        public Integer getH() {
            return h;
        }
        
        public Integer getPitch() {
            return pitch;
        }
        
        public Integer getRoll() {
            return roll;
        }
        
        public Integer getW() {
            return w;
        }
        
        public Integer getX() {
            return x;
        }
        
        public Integer getY() {
            return y;
        }
        
        public Integer getYaw() {
            return yaw;
        }
        
        public Integer getZ() {
            return z;
        }
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            if (x != null) {
                json.put("x", x);
            }
            if (y != null) {
                json.put("y", y);
            }
            if (z != null) {
                json.put("z", z);
            }
            if (w != null) {
                json.put("w", w);
            }
            if (d != null) {
                json.put("d", d);
            }
            if (h != null) {
                json.put("h", h);
            }
            if (roll != null) {
                json.put("roll", roll);
            }
            if (pitch != null) {
                json.put("pitch", pitch);
            }
            
            if (yaw != null) {
                json.put("yaw", yaw);
            }
            if (area != null) {
                json.put("area", area);
            }
            return json;
        }
        
        public boolean updateFromJSON(JSONObject json) {
            boolean dirty = false;
            if (json.has("x")) {
                int newV = json.getInt("x");
                if (x == null || newV != x) {
                    this.x = newV;
                    dirty = true;
                }
            }
            if (json.has("y")) {
                int newV = json.getInt("y");
                if (y == null || newV != y) {
                    this.y = newV;
                    dirty = true;
                }
            }
            if (json.has("z")) {
                int newV = json.getInt("z");
                if (z == null || newV != z) {
                    this.z = newV;
                    dirty = true;
                }
            }
            if (json.has("w")) {
                int newV = json.getInt("w");
                if (w == null || newV != w) {
                    this.w = newV;
                    dirty = true;
                }
            }
            if (json.has("d")) {
                int newV = json.getInt("d");
                if (d == null || newV != d) {
                    this.d = newV;
                    dirty = true;
                }
            }
            if (json.has("h")) {
                int newV = json.getInt("h");
                if (h == null || newV != h) {
                    this.h = newV;
                    dirty = true;
                }
            }
            if (json.has("roll")) {
                int newV = json.getInt("roll");
                if (roll == null || newV != roll) {
                    this.roll = newV;
                    dirty = true;
                }
            }
            if (json.has("pitch")) {
                int newV = json.getInt("pitch");
                if (pitch == null || newV != pitch) {
                    this.pitch = newV;
                    dirty = true;
                }
            }
            if (json.has("yaw")) {
                int newV = json.getInt("yaw");
                if (yaw == null || newV != yaw) {
                    this.yaw = newV;
                    dirty = true;
                }
            }
            if (json.has("area")) {
                String newV = json.getString("area");
                if (area == null || !newV.equals(area)) {
                    this.area = newV;
                    dirty = true;
                }
            }
            
            return dirty;
        }
    }
}