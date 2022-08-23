package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/locations",
        userLevel = 1
)
public class DeviceLocationsServlet extends AuthedServlet {


    public DeviceLocationsServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "locations",
            description = "Get the locations of all device you have access to",

            responses = {
                    @ApiResponse(

                            responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
                            description = "A JSONObject with name value pairs of devices and its locations",

                            content = @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(
                                            type = SchemaType.OBJECT,additionalProperties = "{\"$ref\": \"#/components/schemas/deviceLocation\"}"


                                    ),
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

            throws IOException, NoAccessToScope, DeviceNotFoundException, ParameterNotFoundException {
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

