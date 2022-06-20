package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/schreibtrainer/add"}, loadOnStartup = 1)
public class SchreibtrainerAddServlet extends AuthedServlet {
    
    
    public SchreibtrainerAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws NotObjectSchemaException,ParameterNotFoundException {
        
        
        JSONObject o = request.getBodyAsJSONObject();
        try {
            if ( o.has("uuid")) {
                try {
                    controller.getDevice(o.getString("uuid"));
                    return;
                } catch (DeviceNotFoundException ex ){
                    JSONObject dev = new JSONObject();
                    dev.put("uuid",o.getString("uuid"));
                    controller.parseAddDevice(this.server,o,this.getSource(request), request.getUser());
                }

            }
            //controller.parseAddDevice(this.server,o,this.getSource(request), getUser(request));
            response.setStatus(STATUS_NO_RESPONSE);
            return;
        }  catch (Exception e) {
            e.printStackTrace();
            response.setStatus(STATUS_UNPROCESSABLE);
            
        }
    
        
    }
    
    
}

