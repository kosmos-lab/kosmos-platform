package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;


@ApiEndpoint(
        path = "/device/settext",
        userLevel = 1
)
public class DeviceSetTextServlet extends KosmoSAuthedServlet {
    public DeviceSetTextServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    @Operation(
            tags = {"device"},
            summary = "settext",
            description = "adds a text element to the device ",
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
                                                    name = "key",
                                                    schema = @Schema(
                                                            description = "The key of the text to add",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "value",
                                                    schema = @Schema(
                                                            description = "The value of the text to add",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),

                                    }, examples = {
                                    @ExampleObject(
                                            name = "add description to multi2",
                                            value = "{\"key\":\"description\",\"value\":\"some text\",\"uuid\":\"multi2\"}"
                                    )

                            }

                            )
                    }
            ),
            responses = {
                                       @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The text was added"),            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
 throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope, UnauthorizedException {
        
        
        Device d = controller.getDevice(request.getUUID());
        
        controller.addDeviceText(d, request.getString("key"),request.getString("value"));

        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
        
        
    }
    
    
}
    
    


