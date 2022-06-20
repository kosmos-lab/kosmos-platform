package de.kosmos_lab.kosmos.platform.web.servlets.server;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/server/stop"}, loadOnStartup = 1)
public class ServerStopServlet extends AuthedServlet {
    
    
    public ServerStopServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 100);
        
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {
        
        (new Thread(this.controller::stop)).start();
        response.setStatus(STATUS_NO_RESPONSE);
        return;
        
    }
    
    
}

