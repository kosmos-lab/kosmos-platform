package de.kosmos_lab.kosmos.platform.web.servlets.mqtt;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/mqtt/auth"}, loadOnStartup = 1)
@Deprecated
public class AuthServlet extends KosmoSServlet {

    
    public AuthServlet(WebServer webServer, IController controller) {
        super(webServer,controller);
    
    }

    @Override
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParameterNotFoundException {
        //super.doPost(request, response);
        String user = request.getParameter("username",true);
        String pass = request.getParameter("password",true);
        
    
        IUser u = controller.tryLogin(user, pass);
        if (u != null) {
            response.setStatus(200);
            return;
        }
        response.setStatus(403);
        return;
        
        
    }
    
}

