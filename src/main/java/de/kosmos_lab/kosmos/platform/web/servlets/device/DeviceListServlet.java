package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.ArraySchema;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/list",
        userLevel = 1
)
public class DeviceListServlet extends AuthedServlet {


    public DeviceListServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "list",
            description = "List all devices (you can read)",

            responses = {
                    @ApiResponse(
                            description = "List of devices",

                            responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(
                                                            ref = "#/components/schemas/deviceInfo"
                                                    )
                                            ),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "example",
                                                            value = "[{\"schema\":\"https://kosmos-lab.de/schema/HSVLamp.json\",\"lastUpdate\":1603442792048,\"name\":\"hsv1\",\"state\":{\"saturation\":100,\"dimmingLevel\":8,\"hue\":285,\"on\":true,\"colorTemperature\":6175},\"uuid\":\"hsv1\"}]"
                                                    )
                                            }
                                    )
                            }
                    ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {

        JSONArray arr = new JSONArray();
        for (Device device : this.controller.getAllDevices()) {
            try {
                IUser user = request.getKosmoSUser();
                if (device.canRead(user)) {
                    arr.put(device.toJSON());
                }
            } catch (NoAccessToScope e) {

            }

        }
        sendJSON(request, response, arr);


    }

}

