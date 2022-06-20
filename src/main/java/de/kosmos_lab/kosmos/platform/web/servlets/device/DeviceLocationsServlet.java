package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
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


@WebServlet(urlPatterns = {"/device/locations"}, loadOnStartup = 1)
public class DeviceLocationsServlet extends AuthedServlet {
    
    
    public DeviceLocationsServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws IOException, NoAccessToScope, DeviceNotFoundException, ParameterNotFoundException {
        JSONObject json = new JSONObject();
        for (Device device : this.controller.getAllDevices()) {
            try {
                IUser user = request.getKosmoSUser();
                if (device.canRead(user)) {
                    Device.Location loc = device.getLocation();
                    if (loc != null) {
                        json.put(device.getUniqueID(), loc.toJSON());
                    }
                }
            } catch (NoAccessToScope e) {
            
            }
        
        }
        sendJSON(request,response,json);
    }
    
}

