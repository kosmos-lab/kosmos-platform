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
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/camera/recording/start",
        userLevel = 1
)
public class CameraStartRecordingServlet extends KosmoSAuthedServlet {
    private static final String FIELD_CAMERA = "camera";


    public CameraStartRecordingServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "Start recording of camera video",
            description = "Start recording a video of the given camera.",
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
                            description = "The camera is now recording"
                    ),

                    
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "This camera is already recording."),

            }
    )

    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, CameraNotFoundException, ParameterNotFoundException , UnauthorizedException {

        String cameraName = request.getParameter(FIELD_CAMERA, true);
        ICamera cam = controller.getCamera(cameraName);
        if (cam.isRecording()) {

            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT);
            return;
        }
        controller.startRecording(cam, request.getKosmoSUser());


        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
        return;
    }

}

