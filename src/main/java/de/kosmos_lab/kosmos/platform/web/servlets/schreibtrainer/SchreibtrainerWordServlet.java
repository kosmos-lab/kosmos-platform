package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.TimedList;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/schreibtrainer/word"}, loadOnStartup = 1)
public class SchreibtrainerWordServlet extends KosmoSServlet {
    
    
    public SchreibtrainerWordServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
        logger.warn("adding SchreibtrainerWordServlet");
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws ParameterNotFoundException {
        
        String uuid = request.getParameter("uuid");
        //logger.info("length of content: {}",request.getContentLength());
        JSONObject o = request.getBodyAsJSONObject();
        if (o == null) {
            throw new ParameterNotFoundException("body");
        }
        if (uuid == null) {
            uuid = o.optString("uuid", null);
            
        }
        if (uuid == null) {
            throw new ParameterNotFoundException("uuid");
        }
        
        
        logger.warn("got new word {} on {}: ", uuid, o.getString("text"));
        try {
            Device device = SchreibtrainerConstants.getDevice(this.controller, server, uuid);
            TimedList wl = SchreibtrainerConstants.getWordList(this.controller, server, device);
            wl.addEntry(o.getString("text"));
            //device.set("wordList", wl.toJSONArray(), false);
            
            device.updateFromJSON(this.server,new JSONObject().put("text", String.join(" ",wl.toStringList())).put("wordList",wl.toJSONArray()),controller.getSource(SchreibtrainerConstants.SOURCENAME));
            sendJSON(request, response, o);
            return;
            
        } catch (SchemaNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(STATUS_UNPROCESSABLE);
            return;
            
        }
        
        response.setStatus(STATUS_ERROR);
        return;
        
    }
    
    
}

