package de.kosmos_lab.kosmos.platform.web.servlets.server;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/server/alive"}, loadOnStartup = 1)
public class ServerAliveServlet extends KosmoSServlet {


    public ServerAliveServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
        
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) {


        try {
            sendText(request,response,"online");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    
    
}

