package de.dfki.baall.helper.webserver.servlets.user;

import de.dfki.baall.helper.webserver.AuthedServlet;
import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.WebServer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/user/view"}, loadOnStartup = 1)

public class UserViewServlet extends AuthedServlet {
    public UserViewServlet(WebServer webServer) {
        super(webServer, 10);
    }
    
    public void get(MyHttpServletRequest request, HttpServletResponse response) throws IOException {
        sendText(request, response, "Hello - you are worthy of seeing this!");
        
    }
    
}
