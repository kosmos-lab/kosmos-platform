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


@WebServlet(urlPatterns = {"/device/delete"}, loadOnStartup = 1)
public class DeviceDeleteServlet extends AuthedServlet {
    
    
    public DeviceDeleteServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response) throws ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope {
        
        
        String id = request.getUUID();
        
        
        Device d = controller.getDevice(id);
        if (d == null) {
            throw new DeviceNotFoundException(id);
        }
        IUser u = request.getKosmoSUser();
        if (u != null) {
            if (d.hasDelScope()) {
                if (d.canDel(u)) {
                    
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    
                    return;
                }
            } else {
                if (getSource(request).getSourceName().equalsIgnoreCase(d.getSource().getSourceName())) {
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    
                    return;
                } else if (u.isAdmin()) {
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    
                    return;
                    
                }
            }
            
            
            response.setStatus(STATUS_FORBIDDEN);
            return;
        }
        
        
    }
    
    
}

