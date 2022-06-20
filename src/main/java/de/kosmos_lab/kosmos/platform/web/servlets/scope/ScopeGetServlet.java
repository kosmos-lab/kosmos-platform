package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
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
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/scope/get"}, loadOnStartup = 1)
public class ScopeGetServlet extends AuthedServlet {
    
    
    public ScopeGetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ScopeNotFoundException, ParameterNotFoundException {
        
        JSONObject body = null;
        
        
        try {
            String sname = request.getString("name");
            
            try {
                Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                sendJSON(request, response, scope.toJSON());
                return;
            } catch (NotFoundInPersistenceException ex) {
                throw new ScopeNotFoundException(sname);
            }
            
            
        } catch (ParameterNotFoundException ex) {
        
        }
        int id = request.getInt("id");
        try {
            Scope scope = controller.getScope(id, CacheMode.CACHE_AND_PERSISTENCE);
            sendJSON(request, response, scope.toJSON());
            return;
        } catch (NotFoundInPersistenceException ex) {
            throw new ScopeNotFoundException("" + id);
        }
        
        
        
    }
    
    
}

