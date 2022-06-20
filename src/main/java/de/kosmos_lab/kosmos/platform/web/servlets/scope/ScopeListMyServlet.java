package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(urlPatterns = {"/scope/my"}, loadOnStartup = 1)

public class ScopeListMyServlet extends AuthedServlet {
    
    
    public ScopeListMyServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException {
        
        JSONObject o = new JSONObject();
        
        JSONArray arr = new JSONArray();
        for (Scope s : this.controller.getAllScopesWithUser(request.getKosmoSUser())) {
            
            arr.put(s.getName());
            
        }
        o.put("user",arr);
        arr = new JSONArray();
        for (Scope s : this.controller.getAllScopesWithAdmin(request.getKosmoSUser())) {
        
            arr.put(s.getName());
        
        }
        o.put("admin",arr);
        sendJSON(request, response, o);
        
        
    }
}
