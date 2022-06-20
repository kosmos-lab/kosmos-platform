package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/device/add"}, loadOnStartup = 1)
public class DeviceAddServlet extends AuthedServlet {
    
    
    public DeviceAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws NotObjectSchemaException, ParameterNotFoundException, IOException, DeviceAlreadyExistsException, SchemaNotFoundException {
        
        
        JSONObject o = request.getBodyAsJSONObject();
        if ( o != null ) {
            
                //logger.info("type of request: {}", request.getClass());
                controller.parseAddDevice(this.server, o, this.getSource(request), request.getKosmoSUser());
                response.setStatus(STATUS_NO_RESPONSE);
        
            
        }
        
    }
    
    
}

