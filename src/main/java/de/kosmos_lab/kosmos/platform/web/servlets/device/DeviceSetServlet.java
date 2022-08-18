package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/set",
        userLevel = 1
)
public class DeviceSetServlet extends AuthedServlet {
    
    
    public DeviceSetServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "set",
            description = "set the properties of a device",
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


                                    }, examples = {
                                    @ExampleObject(
                                            name = "update multi2",
                                            value = "{\"uuid\":\"multi2\",\"currentEnvironmentTemperature\":17,\"humidityLevel\":45}"
                                    ),
                                    @ExampleObject(
                                            name = "hsv1 on",
                                            value = "{\"uuid\":\"hsv1\",\"on\":true}"
                                    ),
                                    @ExampleObject(
                                            name = "hsv1 aqua",
                                            value = "{\"uuid\":\"hsv1\",\"on\":true,\"hue\":180,\"saturation\":100}"
                                    ),
                                    @ExampleObject(
                                            name = "hsv1 yellow",
                                            value = "{\"uuid\":\"hsv1\",\"on\":true,\"hue\":60,\"saturation\":100}"
                                    ),
                                    @ExampleObject(
                                            name = "hsv1 warmwhite",
                                            value = "{\"uuid\":\"hsv1\",\"on\":true,\"colorTemperature\":7000}"
                                    ),
                                    @ExampleObject(
                                            name = "hsv1 coldwhite",
                                            value = "{\"uuid\":\"hsv1\",\"on\":true,\"colorTemperature\":1500}"
                                    )

                            }

                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK), ref = "#/components/responses/deviceGet"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
                    // @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),

            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope {
        
        
        String id = request.getParameter("uuid");
        
        if (id == null) {
            id = request.getParameter("id");
            
        }
        if (id != null) {
            
            JSONObject o = request.getBodyAsJSONObject();
            
            Device d = controller.parseSet(this.server, id, o, this.getSource(request), request.getKosmoSUser());
            sendJSON(request, response, d.toJSON());
            return;
            
            
        }
        throw new ParameterNotFoundException("uuid");
        
        
    }
    
    
}

