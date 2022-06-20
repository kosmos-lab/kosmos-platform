package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


@WebServlet(urlPatterns = {"/camera/list"}, loadOnStartup = 1)
public class CameraListServlet extends AuthedServlet {


    public CameraListServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        boolean details = false;
        try {
            details = request.getBoolean("details");

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONArray arr = new JSONArray();
        for (ICamera camera : this.controller.getAllCameras()) {
            JSONObject json = new JSONObject().put("name", camera.getName()).put("recording", camera.isRecording());
            if (details) {
                JSONArray recordings = new JSONArray();
                for (File f : controller.listRecordings(camera, request.getKosmoSUser())) {
                    recordings.put(new JSONObject()
                            .put("name", f.getName())
                            .put("size", f.length())
                    );
                }
                json.put("recordings", recordings);
            }
            arr.put(json);

        }
        sendJSON(request, response, arr);


    }

}

