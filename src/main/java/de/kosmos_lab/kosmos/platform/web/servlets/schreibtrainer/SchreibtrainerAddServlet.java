package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.annotations.tags.Tag;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;

import static de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer.SchreibtrainerConstants.FIELD_UUID;


@Tag(
        name = "schreibtrainer",
        description = "Schreibtrainer"
)
@ApiEndpoint(
        path = "/schreibtrainer/add",
        userLevel = 1
)

public class SchreibtrainerAddServlet extends AuthedServlet {


    public SchreibtrainerAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }

    @Operation(
            tags = {"schreibtrainer"},
            summary = "add",
            description = "Add a new pen to the system",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_UUID,
                                                    schema = @Schema(
                                                            description = "The UUID of the pen to add",
                                                            type = SchemaType.STRING,
                                                            required = true
                                                    )
                                            ),


                                    }

                            )
                    }
            ),

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The pen was added successfully"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws NotObjectSchemaException, ParameterNotFoundException {


        JSONObject o = request.getBodyAsJSONObject();
        try {
            if (o.has(FIELD_UUID)) {
                SchreibtrainerConstants.getDevice(controller, server, o.getString(FIELD_UUID));
                response.setStatus(STATUS_NO_RESPONSE);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();


        }
        response.setStatus(STATUS_ERROR);


    }


}

