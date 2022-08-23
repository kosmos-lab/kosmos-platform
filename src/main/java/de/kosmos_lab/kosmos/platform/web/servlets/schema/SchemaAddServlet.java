package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static de.kosmos_lab.kosmos.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_ALL;
import static de.kosmos_lab.kosmos.platform.web.servlets.schema.SchemaGetServlet.SCHEMA_PERSON;

@ApiEndpoint(
        path = "/schema/add",
        userLevel = 1

)
public class SchemaAddServlet extends AuthedServlet {


    public SchemaAddServlet(WebServer webServer, IController controller, int level) {
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

                            responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, AlreadyExistsException {
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
                    response.getWriter().print(o);
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_OK);
                    return;
                }
            }
        }
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
    }


}

