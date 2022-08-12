package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.ArraySchema;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.ObjectSchema;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ApiResponseDescription;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

@ApiEndpoint(
        path = "/camera/list",
        userLevel = 1
)
@ApiResponseDescription(name= "CameraNotFoundError",description = "The camera was not found")

@ObjectSchema(
        componentName = "camera",
        properties = {
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the camera",
                                type = SchemaType.STRING,
                                required = true

                        )
                ),
                @SchemaProperty(
                        name = "recording",
                        schema = @Schema(
                                description = "Is the camera currently recording?",
                                type = SchemaType.BOOLEAN,
                                required = true
                        )
                ),
                @SchemaProperty(
                        name = "recordings",
                        array = @ArraySchema(
                                uniqueItems = true,
                                arraySchema = @Schema(
                                        description = "List of camera recordings",
                                        ref = "#/components/schemas/cameraRecording",
                                        required = false
                                )
                        )

                )
        }
)
public class CameraListServlet extends AuthedServlet {

    public static final String FIELD_DETAILS = "details";

    public CameraListServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "list cameras",
            description = "List all known cameras",
            parameters = {
                    @Parameter(
                            description = "Include more details in the response",
                            in = ParameterIn.QUERY,
                            name = FIELD_DETAILS,
                            schema = @Schema(
                                    type = SchemaType.BOOLEAN,
                                    defaultValue = "false"
                            ),
                            required = false
                    )
            },
            responses = {
                    @ApiResponse(
                            description = "List of cameras",

                            responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(
                                                            ref = "#/components/schemas/camera"
                                                    )
                                            ),
                                            examples = {
                                                    @ExampleObject(name = "example without details", value = "[{\"name\":\"camera1\",\"recording\":true},{\"name\":\"camera2\",\"recording\":false}]"),
                                                    @ExampleObject(name = "example with details", value = "[{\"name\":\"camera1\",\"recording\":true,recordings:[{\"size\": 158373240, \"name\": \"camera1_2022-03-22_17-51-32.mp4\"},{\"size\": 70586289, \"name\": \"camera1_2022-03-22_17-48-51.mp4\"}]},{\"name\":\"camera2\",\"recording\":false,\"recordings\":[]}]")
                                            }
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        boolean details = false;
        try {
            details = request.getBoolean(FIELD_DETAILS);

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

