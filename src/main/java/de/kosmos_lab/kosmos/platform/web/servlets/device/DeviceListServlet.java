package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONArray;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/device/list"}, loadOnStartup = 1)
public class DeviceListServlet extends AuthedServlet {
    
    
    public DeviceListServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        
        JSONArray arr = new JSONArray();
        for (Device device : this.controller.getAllDevices()) {
            try {
                IUser user = request.getKosmoSUser();
                if (device.canRead(user)) {
                    arr.put(device.toJSON());
                }
            } catch (NoAccessToScope e) {
            
            }
            
        }
        sendJSON(request, response, arr);
        
        
    }
    
}

