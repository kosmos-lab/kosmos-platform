package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/list",
        userLevel = 1
)
public class DeviceListServlet extends KosmoSAuthedServlet {


    public DeviceListServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "list",
            description = "List all devices (you can read)",

            responses = {
                    @ApiResponse(
                            description = "List of devices",

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
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
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
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

