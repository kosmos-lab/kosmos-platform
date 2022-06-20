package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.gesture.data.Gesture;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/gesture/guess"}, loadOnStartup = 1)
public class GestureGuessServlet extends KosmoSServlet {


    public GestureGuessServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        Gesture g = controller.getGestureProvider().predict(request.getBodyAsJSONArray());
        if (g != null) {


            JSONObject obj = new JSONObject();

            obj.put("result", g.name);
            obj.put("key", g.Id);
            sendJSON(request, response, obj);
        }
    }

}

