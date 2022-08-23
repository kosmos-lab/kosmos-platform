package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.ArraySchema;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.ObjectSchema;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
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
import jakarta.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

@ObjectSchema(
        componentName = "cameraRecording",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the file",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "size",
                        schema = @Schema(
                                description = "The size of the file in bytes",
                                type = SchemaType.NUMBER,
                                required = true

                        )
                )
        }
)
@ApiEndpoint(
        path = "/camera/recording/list",
        userLevel = 1
)
public class CameraListRecordingServlet extends AuthedServlet {


    private static final String FIELD_CAMERA = "camera";

    public CameraListRecordingServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "List available camera recordings",
            description = "List the recordings of a camera, only shows the recordings you have access to - the recordings you started yourself.",
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
                            description = "List of camera recordings",
                            responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(
                                                            ref = "#/components/schemas/cameraRecording"
                                                    )
                                            ),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "example",
                                                            value = "[{\"size\": 158373240, \"name\": \"camera1_2022-03-22_17-51-32.mp4\"},{\"size\": 70586289, \"name\": \"camera1_2022-03-22_17-48-51.mp4\"}]"
                                                    )
                                            }
                                    )
                            }
                    ),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, CameraNotFoundException, ParameterNotFoundException {

        String cameraName = request.getParameter(FIELD_CAMERA, true);
        ICamera cam = controller.getCamera(cameraName);

        JSONArray arr = new JSONArray();
        for (File f : controller.listRecordings(cam, request.getKosmoSUser())) {
            arr.put(new JSONObject()
                    .put("name", f.getName())
                    .put("size", f.length())
            );
        }
        sendJSON(request, response, arr);

        return;


    }

}

