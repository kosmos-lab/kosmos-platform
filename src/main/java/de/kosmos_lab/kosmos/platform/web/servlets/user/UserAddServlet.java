package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.KosmoSUser;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.utils.StringFunctions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/user/add"}, loadOnStartup = 1)
public class UserAddServlet extends AuthedServlet {
    
    
    public UserAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 100);
        
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws IOException, ParameterNotFoundException {
        String user = request.getString("user");
        String pass = request.getString("pass");
        
        int level = request.getInt("level", 1);
        logger.info("trying to add user {} with level {}",user,level);
        IUser me = request.getKosmoSUser();
        if (level >= me.getLevel()) {
            response.setStatus(STATUS_FORBIDDEN);
            
            return;
            
        }
        logger.info("going to add user {} with level {}",user,level);
    
        IUser u = this.controller.getUser(user);
        if (u == null) {
            logger.info("user is not yet taken {} with level {}",user,level);
            String salt = StringFunctions.generateRandomKey();
            String hash = controller.getPasswordHash(pass, salt);
            u = new KosmoSUser(controller, user, 0, level, hash, salt);
            controller.addUser(u);
            logger.info("finished adding user {} with level {}",user,level);
            response.setStatus(STATUS_NO_RESPONSE);
            return;
        }
        response.setStatus(STATUS_CONFLICT);
        
        
    }
    
    
}

