package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Device;

import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;


import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/set",
        userLevel = 1
)
public class DeviceSetServlet extends KosmoSAuthedServlet {
    
    
    public DeviceSetServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                                       @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), ref = "#/components/responses/deviceGet"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
 throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope, UnauthorizedException {
        
        
        String id = request.getParameter("uuid");
        
        if (id == null) {
            id = request.getParameter("id");
            
        }
        if (id != null) {
            
            JSONObject o = request.getBodyAsJSONObject();
            logger.info("device/set: {}",o);
                //try {
                    Device d = controller.parseSet(this.server, id, o, this.getSource(request), request.getKosmoSUser());
                    sendJSON(request, response, d.toJSON());
                /*} catch (Exception ex) {
                //we just need to know where it crashes...
                    ex.printStackTrace();
                    throw ex;
                }*/


            return;
            
            
        }
        throw new ParameterNotFoundException("uuid");
        
        
    }
    
    
}

