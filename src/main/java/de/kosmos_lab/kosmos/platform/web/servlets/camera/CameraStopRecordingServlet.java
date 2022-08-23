package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.CameraNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/camera/recording/stop",
        userLevel = 1
)public class CameraStopRecordingServlet extends AuthedServlet {

    private static final String FIELD_CAMERA = "camera";


    public CameraStopRecordingServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "Stop recording of camera video",
            description = "Stops the currently running recording of the given camera. You can only stop a recording if you started the recording, or if you are an admin.",
            parameters = {
                    @Parameter(
                            description = "The name of the camera",
                            in = ParameterIn.QUERY,
                            name = FIELD_CAMERA,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            ),
                            required = true
                    )

            },
            responses = {
                    @ApiResponse(
                            
                            responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE),
                            description = "The recording is now stopped."
                    ),
                    
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_CONFLICT), description = "This camera is not recording at the moment."),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), description = "You are not allowed to stop this recording."),

            }
    )
        public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, CameraNotFoundException, NoAccessException, ParameterNotFoundException {

        String cameraName = request.getParameter(FIELD_CAMERA, true);

        ICamera cam = controller.getCamera(cameraName);
        if ( !cam.isRecording()) {

            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_CONFLICT);
            return;
        }
        controller.stopRecording(cam, request.getKosmoSUser());

        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_OK);
        return;


    }

}

