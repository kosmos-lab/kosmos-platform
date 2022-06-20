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


@WebServlet(urlPatterns = {"/schema/get"}, loadOnStartup = 1)
public class SchemaGetServlet extends AuthedServlet {
    
    
    public SchemaGetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, ParameterNotFoundException {
        String id = request.getString("id");
        
        try {
            DataSchema s = controller.getSchema(id);
            if (s != null) {
                sendJSON(request, response, s.getRawSchema());
                return;
            }
        } catch (SchemaNotFoundException ex) {
            response.setStatus(STATUS_NOT_FOUND);
            return;
        }
        
        //throw new SchemaNotFoundException(id);
        
        
    }
    
    
}

