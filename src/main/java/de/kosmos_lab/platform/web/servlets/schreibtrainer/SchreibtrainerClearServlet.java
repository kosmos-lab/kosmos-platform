package de.kosmos_lab.platform.web.servlets.schreibtrainer;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.TimedList;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;


@ApiEndpoint(
        path = "/schreibtrainer/clear",
        userLevel = 1
)
public class SchreibtrainerClearServlet extends KosmoSAuthedServlet {


    public SchreibtrainerClearServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"schreibtrainer"},
            summary = "clear",
            description = "clear words from pen",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = SchreibtrainerConstants.FIELD_UUID,
                                                    schema = @Schema(
                                                            description = "The UUID of the pen to clear",
                                                            type = SchemaType.STRING,
                                                            required = true
                                                    )
                                            ),


                                    }

                            )
                    }
            ),

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The pen was cleared successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE), description = "The device has not the correct schema to be a pen"),

            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ParameterNotFoundException {

        // String secret = request.getString("token");

        String uuid = request.getString(SchreibtrainerConstants.FIELD_UUID);


        try {
            Device device = SchreibtrainerConstants.getDevice(this.controller, server, uuid);
            if (device.getSchema().getId().equalsIgnoreCase(SchreibtrainerConstants.Schema)) {
                TimedList wl = SchreibtrainerConstants.getWordList(this.controller, server, device);
                wl.clear();
                //device.set("wordList", wl.toJSONArray(), false);
                device.updateFromJSON(this.server, new JSONObject().put("text", "").put("wordList", wl.toJSONArray()), controller.getSource(SchreibtrainerConstants.SOURCENAME));
                sendJSON(request, response, new JSONObject());
                return;
            }
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE);
            sendText(request, response, "Device " + uuid + " does not have schema: " + SchreibtrainerConstants.Schema);
            return;
        } catch (SchemaNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_ERROR);
            return;

        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_ERROR);


    }


}

