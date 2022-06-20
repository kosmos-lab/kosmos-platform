package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/device/location"}, loadOnStartup = 1)
public class DeviceLocationServlet extends AuthedServlet {
    
    
    public DeviceLocationServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, NoAccessToScope, DeviceNotFoundException, ParameterNotFoundException {
        String uuid = request.getUUID();
        
        
        Device.Location loc = controller.getLocation(request.getKosmoSUser(), uuid);

        if (loc != null) {
            sendJSON(request, response, loc.toJSON());
        } else {
            sendJSON(request, response, new JSONObject());
        }
        
        
    
    
    
}
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope {
        
        
        JSONObject o = request.getBodyAsJSONObject();
        if (!o.has("uuid")) {
            try {
                o.put("uuid",request.getString("uuid"));
            } catch (de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException e) {
                e.printStackTrace();
            }
        }
        sendJSON(request, response, this.controller.setLocation(request.getKosmoSUser(), o,this.getSource(request)).toJSON());
        
        //response.setStatus(STATUS_NO_RESPONSE);
        
        
    }
    
}

