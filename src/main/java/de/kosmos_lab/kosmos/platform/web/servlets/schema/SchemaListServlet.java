package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.kosmos_lab.kosmos.data.DataSchema;
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


@WebServlet(urlPatterns = {"/schema/list"}, loadOnStartup = 1)
public class SchemaListServlet extends AuthedServlet {
    
    
    public SchemaListServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException {
        
        JSONArray arr = new JSONArray();
        for (DataSchema d : this.controller.getAllSchemas()) {
            JSONObject o = new JSONObject(d.getRawSchema().toMap());
            o.remove("examples");
            o.remove("failures");
            arr.put(o);
            
        }
        sendJSON(request, response, arr);
        
        
    }
    
}

