package de.kosmos_lab.platform.web.servlets.schreibtrainer;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.TimedList;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class SchreibtrainerConstants {

    public static final String Schema = "https://kosmos-lab.de/schema/StabiloPen2.json";
    public static final long wordtimeout = 10000l;
    public static final String SOURCENAME = "stabilo";
    public static final String FIELD_UUID = "uuid";

    public static Device getDevice(IController controller, KosmoSWebServer server, String uuid) throws SchemaNotFoundException, DeviceAlreadyExistsException, ParameterNotFoundException, DeviceNotFoundException {
        try {
            return controller.getDevice(uuid);
        } catch (DeviceNotFoundException ex) {

            JSONObject dev = new JSONObject();
            dev.put("schema", SchreibtrainerConstants.Schema);
            dev.put("uuid", uuid);
            dev.put("state", new JSONObject().put("text","").put("wordList", new JSONArray()));
            controller.parseAddDevice(server, dev, controller.getSource(SOURCENAME), controller.getUserCreateIfUnavailable("stabilo"));
            Device device = controller.getDevice(uuid);
            //device.set("wordList", new TimedList(SchreibtrainerConstants.wordtimeout), false);
            //device.set("wordList", new JSONArray(), false);
            return device;


        }

    }

    public static TimedList getWordList(IController controller, KosmoSWebServer server, Device device) {


        Object wl = device.getHidden("wordList");
        if (wl == null) {
            TimedList wll = new TimedList(SchreibtrainerConstants.wordtimeout);
            device.setHidden("wordList", wll);
            wl = wll;
        }

        return (TimedList) wl;


    }
}
