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

@WebServlet(urlPatterns = {"/camera/snapshot"}, loadOnStartup = 1)
public class CameraSnapshotServlet extends AuthedServlet {


    public CameraSnapshotServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, CameraNotFoundException, ParameterNotFoundException {

        String cameraName = request.getParameter("camera", true);
        ICamera cam = controller.getCamera(cameraName);
        int width = request.getInt("width", 0);
        int height = request.getInt("height", 0);
        byte[] content;
        if (height != 0 && width != 0) {
            content = cam.getSnapshot(width, height);
        } else {
            content = cam.getSnapshot();
        }



                /*
                actually not needed ...
                InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));
                String mimeType = URLConnection.guessContentTypeFromStream(is);
                response.setHeader("Content-Type", mimeType);
                if (mimeType == null ) {
                    mimeType = "image/jpeg";
                }
                logger.info("found mime type {}",mimeType);

                 */


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

