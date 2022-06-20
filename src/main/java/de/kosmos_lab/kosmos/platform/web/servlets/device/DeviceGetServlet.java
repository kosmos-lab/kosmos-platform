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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/device/get"}, loadOnStartup = 1)
public class DeviceGetServlet extends AuthedServlet {
    
    
    public DeviceGetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope {
        
        
        String id = request.getUUID();
        
        
        Device device = controller.getDevice(id);
        if (device == null) {
            throw new DeviceNotFoundException(id);
        }
        IUser user = request.getKosmoSUser();
        if (device.canRead(user)) {
            sendJSON(request, response, device.toJSON());
            
            return;
        }
        
        
    }
    
    
}

