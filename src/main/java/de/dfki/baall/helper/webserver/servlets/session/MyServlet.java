package de.dfki.baall.helper.webserver.servlets.session;

import de.dfki.baall.helper.persistence.ISesssionPersistence;
import de.dfki.baall.helper.persistence.exceptions.NoPersistenceException;
import de.dfki.baall.helper.webserver.AuthedServlet;
import de.dfki.baall.helper.webserver.MyHttpServletRequest;
import de.dfki.baall.helper.webserver.WebServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@WebServlet(urlPatterns = {"/session/my"}, loadOnStartup = 1)

public class MyServlet extends AuthedServlet {
    public MyServlet(WebServer webServer) {
        super(webServer, 1);
    }
    
    public void get(MyHttpServletRequest request, HttpServletResponse response) throws IOException, NoPersistenceException {
        Collection<JSONObject> list = server.getPersistence(ISesssionPersistence.class).getMySessions(request.getUser().getName());
        JSONArray arr = new JSONArray();
        for (JSONObject o : list) {
            arr.put(o.get("jwtid"));
        }
        sendJSON(request, response, arr);
        
    }
    
}
