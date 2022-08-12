package de.dfki.baall.helper.webserver.servlets;

import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.MyServlet;
import de.dfki.baall.helper.webserver.WebServer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/hello_world"}, loadOnStartup = 1)
public class HelloWorldServlet extends MyServlet {
    public HelloWorldServlet(WebServer server) {
        super(server);
    }
    
    public void get(MyHttpServletRequest request, HttpServletResponse response)
            throws IOException {
        sendText(request, response, "Hello you");
    }
}
