package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.exceptions.CameraNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/camera/recording/start"}, loadOnStartup = 1)
public class CameraStartRecordingServlet extends AuthedServlet {


    public CameraStartRecordingServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, CameraNotFoundException, ParameterNotFoundException {

        String cameraName = request.getParameter("camera", true);
        ICamera cam = controller.getCamera(cameraName);
        controller.startRecording(cam, request.getKosmoSUser());
        response.setStatus(STATUS_OK);
        return;


    }

}

