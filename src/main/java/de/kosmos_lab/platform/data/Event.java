package de.kosmos_lab.platform.data;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.smarthome.EventInterface;
import de.kosmos_lab.utils.DoubleFunctions;
import de.kosmos_lab.web.data.IUser;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is used as an actual Device. It holds the current states, scopes and so on.
 */
public class Event {
    protected static final Logger logger = LoggerFactory.getLogger("Event");
    private final IController controller;
    private final JSONObject json;
    private final EventInterface source;
    private final Device device;


    public Event(@Nonnull IController controller, @Nonnull EventInterface source, @Nonnull JSONObject json, Device device) throws ValidationException {
        this.controller = controller;
        this.source = source;
        this.json = json;
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public JSONObject toJSON() {
        return json;
    }
}