package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/location",
        userLevel = 1
)
@ObjectSchema(
        componentName = "deviceLocation",

        properties = {
                @SchemaProperty(
                        name = "x",
                        schema = @Schema(
                                description = "the x coordinate of the device",
                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "y",
                        schema = @Schema(
                                description = "the y coordinate of the device",
                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "z",
                        schema = @Schema(
                                description = "the z coordinate of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "w",
                        schema = @Schema(
                                description = "the width of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "d",
                        schema = @Schema(
                                description = "the depth of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "h",
                        schema = @Schema(
                                description = "the height of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "roll",
                        schema = @Schema(
                                description = "the roll of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "pitch",
                        schema = @Schema(
                                description = "the pitch of the device",

                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "yaw",
                        schema = @Schema(
                                description = "the yaw of the device",
                                type = SchemaType.NUMBER,
                                required = false
                        )
                ),
                @SchemaProperty(
                        name = "area",
                        schema = @Schema(
                                description = "the area the device is located in",
                                type = SchemaType.STRING,
                                required = false
                        )
                ),

        }
)
public class DeviceLocationServlet extends KosmoSAuthedServlet {


    public DeviceLocationServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "get location",
            description = "Get the location of the device",
            parameters = {@Parameter(name = "uuid",
                    in = ParameterIn.QUERY,
                    schema = @Schema(
                            description = "The uuid of the device to get",
                            type = SchemaType.STRING,
                            minLength = 3,
                            required = true
                    )
            )
            },
            responses = {
                    @ApiResponse(

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            description = "the location of the device",

                            content = @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = {
                                            @ExampleObject(
                                                    name = "light1",
                                                    value = "{\"x\":10,\"y\":124,\"z\":24,\"area\":\"livingroom\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "light2",
                                                    value = "{\"x\":145,\"y\":125,\"z\":125,\"roll\":12,\"pitch\":24,\"yaw\":46,\"w\":10,\"d\":10,\"h\":10,\"area\":\"bedroom\"}"
                                            )
                                    },
                                    schema = @Schema(ref = "#/components/schemas/deviceLocation")
                            )
                    ),
                    /*@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),*/
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, NoAccessToScope, DeviceNotFoundException, ParameterNotFoundException, UnauthorizedException {
        String uuid = request.getUUID();


        Device.Location loc = controller.getLocation(request.getKosmoSUser(), uuid);

        if (loc != null) {
            sendJSON(request, response, loc.toJSON());
        } else {
            sendJSON(request, response, new JSONObject());
        }


    }

    @Operation(
            tags = {"device"},
            summary = "set location",
            description = "set the location of the device",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {

                                            @SchemaProperty(
                                                    name = "uuid",
                                                    schema = @Schema(
                                                            description = "The uuid of the device to get",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "location",
                                                    schema = @Schema(ref = "#/components/schemas/deviceLocation", required = true)
                                            )

                                    }, examples = {
                                    @ExampleObject(
                                            name = "moved MultiSensor2 to bedroom",
                                            value = "{\"uuid\":\"multi2\",\"location\":{\"area\":\"bedroom\",\"x\":140,\"y\":120,\"z\":85}}"
                                    ),
                                    @ExampleObject(
                                            name = "MultiSensor17 changed rotation",
                                            value = "{\"uuid\":\"multi2\",\"location\":{\"yaw\":140,\"pitch\":120,\"roll\":85}}"
                                    ),
                            }
                            )
                    }
            ),
            responses = {
                    @ApiResponse(

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            description = "the (full) location of the device - also contains the parameters that were already there and not overwritten by the request",

                            content = @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = {
                                            @ExampleObject(
                                                    name = "light1",
                                                    value = "{\"x\":10,\"y\":124,\"z\":24,\"area\":\"livingroom\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "light2",
                                                    value = "{\"x\":145,\"y\":125,\"z\":125,\"roll\":12,\"pitch\":24,\"yaw\":46,\"w\":10,\"d\":10,\"h\":10,\"area\":\"bedroom\"}"
                                            )
                                    },
                                    schema = @Schema(ref = "#/components/schemas/deviceLocation")
                            )
                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope, UnauthorizedException {


        JSONObject o = request.getBodyAsJSONObject();
        if (!o.has("uuid")) {
            try {
                o.put("uuid", request.getString("uuid"));
            } catch (ParameterNotFoundException e) {
                e.printStackTrace();
            }
        }
        sendJSON(request, response, this.controller.setLocation(request.getKosmoSUser(), o, this.getSource(request)).toJSON());

        //response.setStatus(STATUS_NO_RESPONSE);


    }

}

