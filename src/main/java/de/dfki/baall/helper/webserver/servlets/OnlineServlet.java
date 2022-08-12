package de.dfki.baall.helper.webserver.servlets;

import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.MyServlet;
import de.dfki.baall.helper.webserver.WebServer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/online"}, loadOnStartup = 1)
public class OnlineServlet extends MyServlet {
    public OnlineServlet(WebServer server) {
        super(server);
    }
    
    public void get(MyHttpServletRequest request, HttpServletResponse response)
            throws IOException {
        sendText(request, response, "I am here");
    }
}
