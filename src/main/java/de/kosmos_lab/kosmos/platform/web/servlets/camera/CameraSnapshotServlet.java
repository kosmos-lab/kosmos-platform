package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.CameraNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/camera/snapshot",
        userLevel = 1
)
public class CameraSnapshotServlet extends AuthedServlet {
    private static final String FIELD_CAMERA = "camera";
    private static final String FIELD_HEIGHT = "height";
    private static final String FIELD_WIDTH = "width";

    public CameraSnapshotServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "Get camera snapshot",
            description = "Get a current snapshot from the camera.",
            parameters = {
                    @Parameter(
                            description = "The name of the camera",
                            in = ParameterIn.QUERY,
                            name = FIELD_CAMERA,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            ),
                            required = true
                    ),
                    @Parameter(
                            description = "The maximum height in pixels you want to have the image returned in",
                            in = ParameterIn.QUERY,
                            name = FIELD_HEIGHT,
                            schema = @Schema(
                                    type = SchemaType.INTEGER,
                                    defaultValue = "0"
                            ),
                            required = false
                    ),
                    @Parameter(
                            description = "The maximum width in pixels you want to have the image returned in",
                            in = ParameterIn.QUERY,
                            name = FIELD_WIDTH,
                            schema = @Schema(
                                    type = SchemaType.INTEGER,
                                    defaultValue = "0"
                            ),
                            required = false
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = "image/*"
                                    )
                            },
                            description = "An actual binary image to display."
                    ),
                    
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws IOException, CameraNotFoundException, ParameterNotFoundException {
        String cameraName = request.getParameter(FIELD_CAMERA, true);
        ICamera cam = controller.getCamera(cameraName);
        int width = request.getInt(FIELD_WIDTH, 0);
        int height = request.getInt(FIELD_HEIGHT, 0);
        byte[] content;
        if (height != 0 && width != 0) {
            content = cam.getSnapshot(width, height);
        } else {
            content = cam.getSnapshot();
        }
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

