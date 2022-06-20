package de.dfki.baall.helper.webserver.servlets.user;

import de.dfki.baall.helper.persistence.ISesssionPersistence;
import de.dfki.baall.helper.persistence.IUserPersistence;
import de.dfki.baall.helper.persistence.exceptions.NoPersistenceException;
import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.MyServlet;
import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.data.User;
import de.dfki.baall.helper.webserver.exceptions.LoginFailedException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@WebServlet(urlPatterns = {"/user/login"}, loadOnStartup = 1)

public class LoginServlet extends MyServlet {
    public LoginServlet(WebServer server) {
        super(server);
    }
    
    
    public void post(MyHttpServletRequest request, HttpServletResponse response)
            throws IOException, LoginFailedException, NoSuchAlgorithmException, InvalidKeyException, ParameterNotFoundException, NoPersistenceException {
        
        User user = server.getPersistence(IUserPersistence.class).login(request.getParameter("username", true), request.getParameter("password", true));
        
        
        String jwt = server.getPersistence(ISesssionPersistence.class).getJWT(user);
        sendText(request, response, jwt);
        
        
    }
}
