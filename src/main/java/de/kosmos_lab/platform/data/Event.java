package de.kosmos_lab.platform.data;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.smarthome.EventInterface;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

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