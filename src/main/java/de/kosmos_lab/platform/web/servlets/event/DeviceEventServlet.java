package de.kosmos_lab.platform.web.servlets.event;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Event;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/event",
        userLevel = 1
)
public class DeviceEventServlet extends KosmoSAuthedServlet {


    public DeviceEventServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"event"},
            summary = "device/event",
            description = "send an event that is tied to a device",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    additionalPropertiesSchema = @Schema(additionalProperties = "true"),

                                    schemaProperties = {

                                            @SchemaProperty(
                                                    name = "uuid",
                                                    schema = @Schema(
                                                            description = "The uuid of the device",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "event",
                                                    schema = @Schema(
                                                            ref = "#/components/schemas/event"
                                                    )
                                            ),


                                    }, examples = {


                            }

                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NO_RESPONSE), description = "event received"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope, UnauthorizedException {


        String id = request.getParameter("uuid");

        if (id == null) {
            id = request.getParameter("id");

        }
        if (id != null) {

            JSONObject o = request.getBodyAsJSONObject();
            if (o != null) {

                if (o.has("event")) {
                    Device device = controller.getDevice(id);
                    if (device.canRead(request.getKosmoSUser())) {
                        this.controller.fireEvent(new Event(this.controller, this.server, o.getJSONObject("event"), device), this.server);
                        response.setStatus(WebServer.STATUS_NO_RESPONSE);
                        return;
                    }


                } else {
                    throw new ParameterNotFoundException("event");
                }

            }
            return;


        }
        throw new ParameterNotFoundException("uuid");


    }


}

