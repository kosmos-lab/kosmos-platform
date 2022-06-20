package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
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


@WebServlet(urlPatterns = {"/group/my"}, loadOnStartup = 1)
public class GroupMyServlet extends AuthedServlet {
    
    
    public GroupMyServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope {
        JSONObject o = new JSONObject();
    
        JSONArray arr = new JSONArray();
        for (Group group : this.controller.getAllGroupsWithUser(request.getKosmoSUser())) {
        
            arr.put(group.getName());
        
        }
        o.put("user",arr);
        arr = new JSONArray();
        for (Group group : this.controller.getAllGroupsWithAdmin(request.getKosmoSUser())) {
        
            arr.put(group.getName());
        
        }
        o.put("admin",arr);
        sendJSON(request, response, o);
    }
    
    
}

