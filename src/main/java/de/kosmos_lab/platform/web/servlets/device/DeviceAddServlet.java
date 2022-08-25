package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/device/add",
        userLevel = 1
)

@ObjectSchema(
        componentName = "deviceScopes",
        properties = {
                
                @SchemaProperty(
                        name = "read",
                        schema = @Schema(
                                description = "The scope that has read access to this device",
                                type = SchemaType.STRING,
                                required = false
                        )
                
                ),
                @SchemaProperty(
                        name = "write",
                        schema = @Schema(
                                description = "The scope that has write access to this device",
                                type = SchemaType.STRING,
                                required = false
                        )
                
                ),
                @SchemaProperty(
                        name = "delete",
                        schema = @Schema(
                                description = "The scope that has delete access to this device",
                                type = SchemaType.STRING,
                                required = false
                        )
                
                )
        }
)
public class DeviceAddServlet extends KosmoSAuthedServlet {
    
    
    public DeviceAddServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    
    @Operation(
            tags = {"device"},
            
            summary = "Add a device",
            description = "Add a device to the system.",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "schema",
                                                    schema = @Schema(

                                                            description = "The $id/url of the schema to use. If its a schema not already in the system the $id MUST be a reachable Url describing the schema.",
                                                            type = SchemaType.STRING,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "uuid",
                                                    schema = @Schema(

                                                            description = "The uuid to use for the new device, must be unique.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "name",
                                                    schema = @Schema(

                                                            description = "The name to use for the new device, if no name is set uuid will be used. Does not need to be unique.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = false
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "state",
                                                    schema = @Schema(

                                                            description = "The starting state of the device, needs to contain all required values if the schema has any and needs to be valid against the schema.",
                                                            type = SchemaType.OBJECT,
                                                            defaultValue = "{}",
                                                            required = false
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "scopes",
                                                    schema = @Schema(

                                                            description = "The name of the scope to use for the new device.",
                                                            ref = "#/components/schemas/deviceScopes"
                                                    )
                                            ),
                                    }, examples = {
                                    @ExampleObject(
                                            name = "MultiSensor2",
                                            value = "{\"name\":\"multi2\",\"uuid\":\"multi2\",\"schema\":\"https://kosmos-lab.de/schema/MultiSensor.json\",\"state\":{\"currentEnvironmentTemperature\":17,\"humidityLevel\":10}}"
                                    ),
                                    @ExampleObject(
                                            name = "MultiSensor17 with scopes",
                                            value = "{\"name\":\"kosmos_multi17\",\"uuid\":\"kosmos_multi17\",\"schema\":\"https://kosmos-lab.de/schema/MultiSensor.json\",\"state\":{\"currentEnvironmentTemperature\":17,\"humidityLevel\":10},\"scopes\":{\"read\":\"kosmos:read\",\"write\":\"kosmos:write\",\"del\":\"kosmos:del\"}}"
                                    ),
                                    @ExampleObject(
                                            name = "Lamp1",
                                            value = "{\"name\":\"lamp1\",\"uuid\":\"lamp1\",\"schema\":\"https://kosmos-lab.de/schema/Lamp.json\",\"state\":{\"on\":true}}"
                                    )
                            }
                            
                            )
                    }
            ),
            responses = {

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The device was added successfully"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            

            throws NotObjectSchemaException, IOException, DeviceAlreadyExistsException, SchemaNotFoundException, ParameterNotFoundException {
        
        
        JSONObject o = request.getBodyAsJSONObject();
        if (o != null) {

            controller.parseAddDevice(this.server, o, this.getSource(request), request.getKosmoSUser());

            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
           
        }
        
    }
    
    
}

