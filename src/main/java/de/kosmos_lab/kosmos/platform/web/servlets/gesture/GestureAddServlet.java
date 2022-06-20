package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/gesture/add"}, loadOnStartup = 1)
public class GestureAddServlet extends KosmoSServlet {
    
    
    public GestureAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        JSONObject body = request.getBodyAsJSONObject();
        
        if(controller.getGestureProvider().addGesture(
                body.getString("name")
                ,body.getJSONArray("points"),
                true)) {
            sendJSON(request, response, GestureListServlet.getGestureList(this.controller));
            return;
        }
        throw new ValidationException("the same gesture already exists");
    
    }
    
}

