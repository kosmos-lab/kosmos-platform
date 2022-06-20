package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.utils.StringFunctions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/user/password"}, loadOnStartup = 1)
public class UserPasswordServlet extends AuthedServlet {
    
    
    public UserPasswordServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 1);
        
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, ParameterNotFoundException {
        
        
        String user = request.getString("user");
        String pass = request.getString("pass");
    
    
        IUser u = this.controller.getUser(user);
        if (u != null) {
            if (this.isMeOrAmAdmin(request, u)) {
                String salt = StringFunctions.generateRandomKey();
                String hash = controller.getPasswordHash(pass, salt);
                controller.setUserPassword(u, salt, hash);
                response.setStatus(STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(STATUS_FORBIDDEN);
            return;
            
            
        }
        response.setStatus(STATUS_NOT_FOUND);
        return;
        
        
    }
    
    
}

