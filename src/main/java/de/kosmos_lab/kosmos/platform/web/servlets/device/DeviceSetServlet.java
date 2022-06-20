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


@WebServlet(urlPatterns = {"/device/set"}, loadOnStartup = 1)
public class DeviceSetServlet extends AuthedServlet {
    
    
    public DeviceSetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope {
        
        
        String id = request.getParameter("uuid");
        
        if (id == null) {
            id = request.getParameter("id");
            
        }
        if (id != null) {
            
            JSONObject o = request.getBodyAsJSONObject();
            
            Device d = controller.parseSet(this.server, id, o, this.getSource(request), request.getKosmoSUser());
            sendJSON(request, response, d.toJSON());
            return;
            
            
        }
        throw new ParameterNotFoundException("uuid");
        
        
    }
    
    
}

