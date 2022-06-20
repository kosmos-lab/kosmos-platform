package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToGroup;
import de.kosmos_lab.kosmos.exceptions.NotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.UserNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/group/deladmin"}, loadOnStartup = 1)
public class GroupDelAdminServlet extends AuthedServlet {
    
    
    public GroupDelAdminServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws ServletException, IOException, NotObjectSchemaException,  NotFoundInPersistenceException, NoAccessToGroup, NotFoundException, ParameterNotFoundException {
        String sname = request.getString("group");
        String uname = request.getString("user");
            Group group = controller.getGroup(sname, CacheMode.CACHE_AND_PERSISTENCE);
            
            if (group.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                IUser u = controller.getUser(uname);
                if (u != null) {
                    controller.delGroupAdmin(group, u);
                    response.setStatus(STATUS_NO_RESPONSE);
                    return;
                }
                throw new UserNotFoundException(uname);
                
            } else {
                throw new NoAccessToGroup(group);
            }

        
    }
    
    
}

