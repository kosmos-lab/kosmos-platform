package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.ScopeNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/scope/adduser"}, loadOnStartup = 1)
public class ScopeAddUserServlet extends AuthedServlet {
    
    
    public ScopeAddUserServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ScopeNotFoundException, ParameterNotFoundException {
        String sname = request.getString("scope");
        String uname = request.getString("user");
                try {
                    Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
    
                    if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                        IUser u = controller.getUser(uname);
                        if (u != null) {
                            controller.addScopeUser(scope, u);
                            response.setStatus(STATUS_NO_RESPONSE);
                            return;
                        }
        
                    } else {
                        throw new NoAccessToScope(scope);
                    }
                }catch (NotFoundInPersistenceException ex) {
                    throw new ScopeNotFoundException(sname);
                }
          
    }
    
    
}

