package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/user/delete"}, loadOnStartup = 1)
public class UserDelServlet extends AuthedServlet {
    public UserDelServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 100);
        
    }
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ParameterNotFoundException {
        String user = request.getString("user");
        IUser u = controller.getUser(user);
        if (u == null) {
            response.setStatus(STATUS_NOT_FOUND);
            return;
        }
    
        IUser me = request.getKosmoSUser();
        if (u.getLevel() >= me.getLevel()) {
            response.setStatus(STATUS_FORBIDDEN);
            return;
        }
        controller.deleteUser(u);
        response.setStatus(STATUS_NO_RESPONSE);
        return;
        
        
    }
    
    
}

