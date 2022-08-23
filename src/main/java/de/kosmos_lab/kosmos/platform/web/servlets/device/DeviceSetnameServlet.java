package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;

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


import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/setname",
        userLevel = 1
)
public class DeviceSetnameServlet extends AuthedServlet {
    public DeviceSetnameServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    @Operation(
            tags = {"device"},
            summary = "setname",
            description = "set the name",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
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
                                                    name = "name",
                                                    schema = @Schema(
                                                            description = "The new name of the device",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    )
                                            ),

                                    }, examples = {
                                    @ExampleObject(
                                            name = "MultiSensor2",
                                            value = "{\"name\":\"multi2\",\"uuid\":\"multi2\"}"
                                    )

                            }

                            )
                    }
            ),
            responses = {
                                       @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The name was changed successfully"),            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope {
        
        
        Device d = controller.getDevice(request.getUUID());
        
        controller.setName(d, request.getString("name"));

        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
        
        
    }
    
    
}
    
    


