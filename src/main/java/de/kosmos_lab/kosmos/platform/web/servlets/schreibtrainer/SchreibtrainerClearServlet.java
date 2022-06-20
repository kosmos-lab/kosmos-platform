package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.TimedList;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/schreibtrainer/clear"}, loadOnStartup = 1)
public class SchreibtrainerClearServlet extends KosmoSServlet {
    
    
    public SchreibtrainerClearServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ParameterNotFoundException {
        
        // String secret = request.getString("token");
        
        String uuid = request.getString("uuid");
        
       
        
        try {
            Device device = SchreibtrainerConstants.getDevice(this.controller, server, uuid);
            if (device.getSchema().getId().equalsIgnoreCase(SchreibtrainerConstants.Schema)) {
                TimedList wl = SchreibtrainerConstants.getWordList(this.controller, server, device);
                wl.clear();
                //device.set("wordList", wl.toJSONArray(), false);
                device.updateFromJSON(this.server, new JSONObject().put("text", "").put("wordList", wl.toJSONArray()), controller.getSource(SchreibtrainerConstants.SOURCENAME));
                sendJSON(request, response, new JSONObject());
                return;
            }
            response.setStatus(STATUS_UNPROCESSABLE);
            sendText(request, response, "Device " + uuid + " does not have schema: " + SchreibtrainerConstants.Schema);
            return;
        } catch (SchemaNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(STATUS_UNPROCESSABLE);
            return;
            
        }
        response.setStatus(STATUS_ERROR);
        return;
        
        
    }
    
    
}

