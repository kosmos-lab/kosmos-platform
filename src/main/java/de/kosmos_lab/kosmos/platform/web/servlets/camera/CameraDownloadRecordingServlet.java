package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.kosmos_lab.kosmos.exceptions.NoAccessToRecording;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.utils.StringFunctions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/camera/recording/download"}, loadOnStartup = 1)
public class CameraDownloadRecordingServlet extends AuthedServlet {


    public CameraDownloadRecordingServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, NoAccessToRecording {
        String filename = StringFunctions.filterFilename(request.getParameter("filename"));
        byte[] content = controller.getRecording(request.getKosmoSUser(),filename);
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        try {

            response.getOutputStream().write(content);
            return;
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("exception while parsing camera {}", ex.getMessage(), ex);

        }

    }

}

