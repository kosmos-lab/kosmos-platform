package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@WebServlet(urlPatterns = {"/user/login"}, loadOnStartup = 1)

public class AuthServlet extends KosmoSServlet {
    
    public AuthServlet(WebServer webServer, IController controller) {
        super(webServer,controller);
    
    }
   
    @Override
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParameterNotFoundException {
        String user = request.getString("user");
        String pass = request.getString("pass");
        IUser u = controller.tryLogin(user, pass);
        if (u != null) {
            try {
                sendJWT(request, response, controller.getJwt().sign(u.toJWT()));
                return;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        response.setStatus(403);
        return;
        
        
    }
    
}

