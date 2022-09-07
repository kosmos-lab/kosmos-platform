package de.kosmos_lab.platform.web.servlets.schema;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static de.kosmos_lab.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_ALL;
import static de.kosmos_lab.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_PERSON;

@ApiEndpoint(
        path = "/schema/list",
        userLevel = 1

)
@ObjectSchema(
        
        componentName = "schema",
        
        properties = {
                @SchemaProperty(
                        name = "$id",
                        schema = @Schema(
                                description = "The unique id of the schema",
                                type = SchemaType.STRING,
                                required = true
                        )
                
                ),
                @SchemaProperty(
                        name = "title",
                        schema = @Schema(
                                description = "The title of the schema",
                                type = SchemaType.STRING
                        )
                
                ),
                @SchemaProperty(
                        name = "type",
                        schema = @Schema(
                                description = "The type of the schema",
                                type = SchemaType.STRING, required = true
                        )
                
                ),
                @SchemaProperty(
                        name = "$schema",
                        schema = @Schema(
                                description = "The schema definition, defines what is needed and so on",
                                type = SchemaType.STRING, defaultValue = "http://json-schema.org/draft-07/schema#"
                        )
                
                ),
                @SchemaProperty(
                        name = "examples",
                        array = @ArraySchema(schema = @Schema(type = SchemaType.OBJECT, description = "List of examples for this schema"))
                ),
                @SchemaProperty(
                        name = "failures",
                        array = @ArraySchema(schema = @Schema(type = SchemaType.OBJECT, description = "List of bad examples for this schema, all examples in here should fail"))
                ),
                @SchemaProperty(
                        name = "required",
                        array = @ArraySchema(schema = @Schema(type = SchemaType.STRING, description = "List of required properties"))
                ),
                @SchemaProperty(
                        name = "additionalProperties", schema = @Schema(type = SchemaType.BOOLEAN, defaultValue = "true", description = "Are additional properties allowed?")
                ),
                @SchemaProperty(
                        name = "properties", schema = @Schema(type = SchemaType.OBJECT, defaultValue = "{}", description = "The definition of the actual properties")
                ),
        }
)
public class SchemaListServlet extends KosmoSAuthedServlet {
    
    
    public SchemaListServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    
    @Operation(
            tags = {"schema"},
            summary = "list",
            description = "Lists all known schemas",
            
            responses = {
                    @ApiResponse(
                            description = "List of devices",
                            
                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(
                                                            ref = "#/components/schemas/schema"
                                                    )
                                            ), examples = {
                                            @ExampleObject(
                                                    name = "example",
                                                    value = "[" + SCHEMA_ALL + "," + SCHEMA_PERSON + "]")
                                    }
                                    
                                    )
                            }
                    
                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            
            throws  IOException, UnauthorizedException {
        
        JSONArray arr = new JSONArray();
        for (DataSchema d : this.controller.getAllSchemas()) {
            JSONObject o = new JSONObject(d.getRawSchema().toMap());
            o.remove("examples");
            o.remove("failures");
            arr.put(o);
            
        }
        sendJSON(request, response, arr);
        
        
    }
    
}

