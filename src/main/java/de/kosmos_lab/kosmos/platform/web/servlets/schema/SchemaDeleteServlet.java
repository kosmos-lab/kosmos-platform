package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/schema/delete"}, loadOnStartup = 1)
public class SchemaDeleteServlet extends AuthedServlet {
    
    
    public SchemaDeleteServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, ParameterNotFoundException {
        String id = request.getString("id");
        DataSchema s = controller.getSchema(id);
        
        if (s != null) {
            if (this.controller.getPersistence().getNumberOfDevicesWithSchema(s) == 0) {
                
                this.controller.deleteSchema(s);
                response.setStatus(STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(STATUS_CONFLICT);
            return;
        }
        
        
        throw new SchemaNotFoundException(id);
        
        
    }
    
    
}

