package de.kosmos_lab.platform.web.servlets.schema;

import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.SchemaAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

import static de.kosmos_lab.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_ALL;
import static de.kosmos_lab.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_PERSON;

@ApiEndpoint(
        path = "/schema/add",
        userLevel = 1

)
public class SchemaAddServlet extends KosmoSAuthedServlet {


    public SchemaAddServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"schema"},
            summary = "add",
            description = "Adds a schema to the system",
            requestBody = @RequestBody(
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(
                                            ref = "#/components/schemas/schema"

                                    )
                                  ,examples = {
                                            @ExampleObject(
                                                    name = "Person",
                                                    value = SCHEMA_PERSON
                                            ),
                                            @ExampleObject(
                                                    name = "All",
                                                    value = SCHEMA_ALL
                                            )
                                    }
                            )

                    }

            ),
            responses = {
                    @ApiResponse(
                            description = "Schema definition",

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
                                            schema = @Schema(
                                                    ref = "#/components/schemas/schema"

                                            ), examples = {
                                            @ExampleObject(
                                                    name = "Person",
                                                    value = SCHEMA_PERSON
                                            ),
                                            @ExampleObject(
                                                    name = "All",
                                                    value = SCHEMA_ALL
                                            )
                                    }

                                    )
                            }

                    ),
                   // @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, NotObjectSchemaException,  AlreadyExistsException, UnauthorizedException {
        JSONObject o = request.getBodyAsJSONObject();
        if (o != null) {
            logger.info("schema add {}", o);
            if (o.has("$id")) {
                String id = o.getString("$id");
                if (id != null) {
                    try {

                        DataSchema s = controller.getSchema(id);
                        if (s != null) {
                            throw new SchemaAlreadyExistsException(id);
                        }
                    } catch (SchemaNotFoundException ex) {
                        //throw new SchemaNotFoundException(id);
                    }
                    DataSchema ds = new DataSchema(o);
                    controller.addSchema(ds);
                    sendJSON(request,response,o);
                    return;
                }
            }
        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_FAILED);
    }


}

