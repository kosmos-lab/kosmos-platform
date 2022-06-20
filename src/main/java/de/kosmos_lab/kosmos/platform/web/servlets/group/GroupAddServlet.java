package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.exceptions.GroupAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
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

@WebServlet(urlPatterns = {"/group/add"}, loadOnStartup = 1)
public class GroupAddServlet extends AuthedServlet {
    
    
    public GroupAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, ParameterNotFoundException {
        String sname = request.getString("name");
        
        
        try {
            controller.addGroup(sname, request.getKosmoSUser());
        } catch (GroupAlreadyExistsException ex) {
            response.setStatus(STATUS_CONFLICT);
            return;
        }
        response.setStatus(STATUS_NO_RESPONSE);
        return;
        
    }
    
}

