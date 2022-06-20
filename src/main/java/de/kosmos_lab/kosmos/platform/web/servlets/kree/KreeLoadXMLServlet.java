package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/kree/loadXML"}, loadOnStartup = 1)
public class KreeLoadXMLServlet extends AuthedServlet {
    
    
    public KreeLoadXMLServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 1);
        
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        String xml = this.server.getRulesService().getXML(request.getKosmoSUser());
        if ( xml != null ) {
            response.setStatus(STATUS_OK);
            sendXML(request,response,xml);
            return;
        }
        else {
            sendXML(request, response,"<xml xmlns=\"https://developers.google.com/blockly/xml\">\n</xml>");
            return;
        }
        
    }
    
    
}

