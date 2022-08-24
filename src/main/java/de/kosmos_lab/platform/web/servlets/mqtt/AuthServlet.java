package de.kosmos_lab.platform.web.servlets.mqtt;

import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/mqtt/auth"}, loadOnStartup = 1)
@Deprecated
public class AuthServlet extends KosmoSServlet {

    
    public AuthServlet(KosmoSWebServer webServer, IController controller) {
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

