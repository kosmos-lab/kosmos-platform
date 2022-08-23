package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.TimedList;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;

import static de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer.SchreibtrainerConstants.FIELD_UUID;


@ApiEndpoint(
        path = "/schreibtrainer/clear",
        userLevel = 1
)
public class SchreibtrainerClearServlet extends AuthedServlet {
    
    
    public SchreibtrainerClearServlet(WebServer webServer, IController controller, int level) {
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
                                                    name = FIELD_UUID,
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The pen was cleared successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE), description = "The device has not the correct schema to be a pen"),

            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ParameterNotFoundException {
        
        // String secret = request.getString("token");
        
        String uuid = request.getString(FIELD_UUID);
        
       
        
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
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE);
            sendText(request, response, "Device " + uuid + " does not have schema: " + SchreibtrainerConstants.Schema);
            return;
        } catch (SchemaNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
            return;
            
        }
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
        return;
        
        
    }
    
    
}

