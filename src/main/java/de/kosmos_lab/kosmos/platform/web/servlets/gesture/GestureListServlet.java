package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.gesture.data.Gesture;
import de.kosmos_lab.kosmos.platform.gesture.data.Point;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/gesture/list"}, loadOnStartup = 1)
public class GestureListServlet extends KosmoSServlet {
    
    
    public GestureListServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public static JSONArray getGestureList(IController controller) {
        JSONArray arr = new JSONArray();
        for (Gesture g : controller.getGestureProvider().listGestures()) {
            JSONObject o = new JSONObject();
            o.put("name",g.name);
            o.put("id",g.Id);
            JSONArray pts = new JSONArray();
            for ( Point p : g.rawPoints) {
                pts.put(new JSONArray().put(p.x).put(p.y).put(p.stroke));
            }
            o.put("points",pts);
            arr.put(o);
        
        }
        return arr;
    }
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        
        
        sendJSON(request, response, getGestureList(this.controller));
        
        
    }
    
}

