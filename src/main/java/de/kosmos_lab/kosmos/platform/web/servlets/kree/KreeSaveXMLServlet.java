package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/kree/saveXML"}, loadOnStartup = 1)
public class KreeSaveXMLServlet extends AuthedServlet {
    
    
    public KreeSaveXMLServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 1);
        
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        String xml = request.getBody();
        if ( xml.length() >0) {
            server.getRulesService().saveXML(request.getKosmoSUser(),xml);
            response.setStatus(STATUS_OK);
            
            return;
        }
        response.setStatus(STATUS_FAILED);
        return;
    }
    
    
}

