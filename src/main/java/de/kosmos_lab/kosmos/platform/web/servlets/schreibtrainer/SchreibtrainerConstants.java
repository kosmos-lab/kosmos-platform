package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.TimedList;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class SchreibtrainerConstants {

    public static final String Schema = "https://kosmos-lab.de/schema/StabiloPen.json";
    public static final long wordtimeout = 10000l;
    public static final String SOURCENAME = "stabilo";

    public static Device getDevice(IController controller, WebServer server, String uuid) throws SchemaNotFoundException, DeviceAlreadyExistsException, ParameterNotFoundException, DeviceNotFoundException {
        try {
            return controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {

            JSONObject dev = new JSONObject();
            dev.put("schema", SchreibtrainerConstants.Schema);
            dev.put("uuid", uuid);
            dev.put("state", new JSONObject().put("wordList", new JSONArray()));
            controller.parseAddDevice(server, dev, controller.getSource(SOURCENAME), controller.getUserCreateIfUnavailable("stabilo"));
            Device device = controller.getDevice(uuid);
            device.set("wordList", new TimedList(SchreibtrainerConstants.wordtimeout), false);
            return device;


        }

    }

    public static TimedList getWordList(IController controller, WebServer server, Device device) {


        Object wl = device.getHidden("wordList");
        if (wl == null) {
            TimedList wll = new TimedList(SchreibtrainerConstants.wordtimeout);
            device.setHidden("wordList", wll);
            wl = wll;
        }

        return (TimedList) wl;


    }
}
