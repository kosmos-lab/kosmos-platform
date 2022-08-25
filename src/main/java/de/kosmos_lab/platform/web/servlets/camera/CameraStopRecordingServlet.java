package de.kosmos_lab.platform.web.servlets.camera;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.CameraNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/camera/recording/stop",
        userLevel = 1
)public class CameraStopRecordingServlet extends KosmoSAuthedServlet {

    private static final String FIELD_CAMERA = "camera";


    public CameraStopRecordingServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                            
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE),
                            description = "The recording is now stopped."
                    ),
                    
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "This camera is not recording at the moment."),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), description = "You are not allowed to stop this recording."),

            }
    )
        public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, CameraNotFoundException, NoAccessException, ParameterNotFoundException {

        String cameraName = request.getParameter(FIELD_CAMERA, true);

        ICamera cam = controller.getCamera(cameraName);
        if ( !cam.isRecording()) {

            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT);
            return;
        }
        controller.stopRecording(cam, request.getKosmoSUser());

        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_OK);
        return;


    }

}

