package de.kosmos_lab.platform.web.servlets.camera;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
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
import de.kosmos_lab.web.doc.openapi.ApiResponseDescription;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
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
@ApiResponseDescription(name = "CameraNotFoundError", description = "The camera was not found")

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
public class CameraListServlet extends KosmoSAuthedServlet {

    public static final String FIELD_DETAILS = "details";

    public CameraListServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"camera"},
            summary = "List available cameras",
            description = "List all known cameras.",
            parameters = {
                    @Parameter(
                            description = "Include more details in the response. If true the response will also contain the currently available recordings of the camera (the recordings you made).",
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

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
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

            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, UnauthorizedException {
        boolean details = request.getBoolean(FIELD_DETAILS, false);


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

