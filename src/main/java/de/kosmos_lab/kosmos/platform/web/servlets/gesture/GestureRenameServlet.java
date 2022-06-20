package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/gesture/rename"}, loadOnStartup = 1)
public class GestureRenameServlet extends KosmoSServlet {
    
    
    public GestureRenameServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, ParameterNotFoundException, NotFoundException {
        String from = request.getString("from");
        String to = request.getString("to");
        if (controller.getGestureProvider().renameGesture(from,to)) {
            sendJSON(request, response, GestureListServlet.getGestureList(this.controller));
            return;
        }
        throw new NotFoundException("Gesture " + from + "not found");
        
    }
    
}

