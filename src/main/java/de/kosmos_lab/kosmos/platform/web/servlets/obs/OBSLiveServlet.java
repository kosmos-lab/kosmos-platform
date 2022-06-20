package de.kosmos_lab.kosmos.platform.web.servlets.obs;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.StateUpdates;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


@WebServlet(urlPatterns = {"/obs/live"}, loadOnStartup = 1)
public class OBSLiveServlet extends AuthedServlet {
    
    
    public static final Long DEFAULT_MAX_AGE = 120000l;
    
    public OBSLiveServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope {
        
        JSONObject o = null;
        String id = request.getUUID();
        
        
        String html = request.getString("html", null);
        
        boolean doHTML = html != null;
        String json = request.getString("json", null);
        boolean doJSON = json != null;

        String type = request.getString("type",null);
        if ( type != null) {
            if (type.equals("json")) {
                doJSON = true;
            }
            else if (type.equals("html")) {
                doHTML = true;
            }
        }
        Long maxage = DEFAULT_MAX_AGE;
        try {
            String t = request.getString("maxage", null);
            if (t != null) {
                maxage = Long.parseLong(t) * 1000;
                if (maxage > DEFAULT_MAX_AGE) {
                    maxage = DEFAULT_MAX_AGE;
                }
            }
            
            
        } catch (NumberFormatException ex) {
        
        
        }
        
        
        HashSet<String> uuids = new HashSet<String>();
        for (String u : id.split(",")) {
            if ( u.contains("*")) {
                uuids.addAll(controller.getMatchingUUID(u));
            }
            else {
                uuids.add(u);
            }
            
        }
        HashMap<Device, StateUpdates> list = controller.getUpdates(uuids, maxage);
        if (doHTML) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {
                
                //sb.append(upd.uuid).append(':').append(upd.changes.toString()).append("\n");
                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    sb.append(entry.getKey().getName()).append(':');
                    JSONObject o2 = new JSONObject();
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), entry.getKey().get(change.getKey()));
                    }
                    sb.append(o2).append("<br>");
                }
            }
            sb.append("</html>");
            sendText(request, response, sb.toString().trim());
        } else if (doJSON) {
            JSONObject j = new JSONObject();
            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {
                JSONObject o2 = new JSONObject();
                
                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    //sb.append(entry.getKey()).append(':');
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), new JSONObject().put("value", entry.getKey().get(change.getKey())).put("age", change.getValue()));
                    }
                    j.put(entry.getKey().getUniqueID(), o2);
                }
                
                
            }
            sendJSON(request, response, j);
        } else {
            StringBuilder sb = new StringBuilder();
            
            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {
                
                //sb.append(upd.uuid).append(':').append(upd.changes.toString()).append("\n");
                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    sb.append(entry.getKey().getName()).append(':');
                    JSONObject o2 = new JSONObject();
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), entry.getKey().get(change.getKey()));
                    }
                    sb.append(o2).append('\n');
                }
            }
            sendText(request, response, sb.toString().trim());
        }
        
        
    }
    
    
}

