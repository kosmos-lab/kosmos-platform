package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.ObjectSchema;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;

import de.kosmos_lab.kosmos.data.Device;

import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/get",
        userLevel = 1
)
@ApiResponse(
        componentName = "deviceGet",

        responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE),
        description = "Details about the scope",

        content = @Content(

                mediaType = MediaType.APPLICATION_JSON,
                examples = {
                        @ExampleObject(
                                name = "hsv1",
                                value = "{\"schema\":\"https://kosmos-lab.de/schema/HSVLamp.json\",\"lastUpdate\":1603442792048,\"name\":\"hsv1\",\"state\":{\"saturation\":100,\"dimmingLevel\":8,\"hue\":285,\"on\":true,\"colorTemperature\":6175},\"uuid\":\"hsv1\"}"
                        )
                },
                schema = @Schema(ref = "#/components/schemas/deviceInfo")
        )
)
@ObjectSchema(
        componentName = "deviceInfo",
        properties = {
                @SchemaProperty(
                        name = "state",
                        schema = @Schema(
                                description = "The current state of the device",
                                type = SchemaType.OBJECT,
                                defaultValue = "{}",
                                required = true
                        )

                ),
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the device",
                                type = SchemaType.STRING,
                                required = false
                        )

                ),
                @SchemaProperty(
                        name = "uuid",
                        schema = @Schema(
                                description = "The uuid of the device",
                                type = SchemaType.STRING,
                                required = true
                        )

                ),
                @SchemaProperty(
                        name = "schema",
                        schema = @Schema(
                                description = "The $id/url of the schema used by the device",
                                type = SchemaType.STRING,
                                required = true
                        )

                ),
                @SchemaProperty(
                        name = "lastUpdate",
                        schema = @Schema(
                                description = "The timestamp of the last update, an update also includes setting a state to the state its already in.",
                                type = SchemaType.INTEGER,
                                required = true
                        )

                ),
                @SchemaProperty(
                        name = "lastChange",
                        schema = @Schema(
                                description = "The timestamp of the last change in the state.",
                                type = SchemaType.INTEGER,
                                required = false
                        )

                )

        }
)
public class DeviceGetServlet extends AuthedServlet {


    public DeviceGetServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},

            summary = "Get information about the device",
            description = "Get information about the device",
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), ref = "#/components/responses/deviceGet"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope {


        String id = request.getUUID();


        Device device = controller.getDevice(id);
        if (device == null) {
            throw new DeviceNotFoundException(id);
        }
        IUser user = request.getKosmoSUser();
        if (device.canRead(user)) {
            sendJSON(request, response, device.toJSON());

            return;
        }


    }


}

