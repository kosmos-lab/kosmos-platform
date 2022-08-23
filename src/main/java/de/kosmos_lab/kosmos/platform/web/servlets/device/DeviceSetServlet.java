package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
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

import org.json.JSONObject;


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
                                       @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), ref = "#/components/responses/deviceGet"),
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

