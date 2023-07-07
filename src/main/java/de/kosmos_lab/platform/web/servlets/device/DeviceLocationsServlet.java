package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/locations",
        userLevel = 1
)
public class DeviceLocationsServlet extends KosmoSAuthedServlet {


    public DeviceLocationsServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "locations",
            description = "Get the locations of all device you have access to",

            responses = {
                    @ApiResponse(

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            description = "A JSONObject with name value pairs of devices and its locations",

                            content = @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {@SchemaProperty(schema = @Schema(ref = "#/components/schemas/deviceLocation"))}


                                    ,
                                    examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "{\"light1\":{\"x\":10,\"y\":124,\"z\":24,\"area\":\"livingroom\"},\"light2\":{\"x\":145,\"y\":125,\"z\":125,\"roll\":12,\"pitch\":24,\"yaw\":46,\"w\":10,\"d\":10,\"h\":10,\"area\":\"bedroom\"}}"
                                            ),

                                    }

                            )
                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, NoAccessToScope, DeviceNotFoundException, ParameterNotFoundException, UnauthorizedException {
        JSONObject json = new JSONObject();
        for (Device device : this.controller.getAllDevices()) {
            try {
                IUser user = request.getKosmoSUser();
                if (device.canRead(user)) {
                    Device.Location loc = device.getLocation();
                    if (loc != null) {
                        json.put(device.getUniqueID(), loc.toJSON());
                    }
                }
            } catch (NoAccessToScope e) {

            }

        }
        sendJSON(request, response, json);
    }

}

