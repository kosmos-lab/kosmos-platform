package de.kosmos_lab.platform.web.servlets.camera;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.CameraNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
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
public class CameraListRecordingServlet extends KosmoSAuthedServlet {


    private static final String FIELD_CAMERA = "camera";

    public CameraListRecordingServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
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

