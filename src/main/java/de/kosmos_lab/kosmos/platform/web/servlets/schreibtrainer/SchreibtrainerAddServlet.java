package de.kosmos_lab.kosmos.platform.web.servlets.schreibtrainer;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.annotations.tags.Tag;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
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


    public SchreibtrainerAddServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The pen was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws NotObjectSchemaException, ParameterNotFoundException {


        JSONObject o = request.getBodyAsJSONObject();
        try {
            if (o.has(FIELD_UUID)) {
                SchreibtrainerConstants.getDevice(controller, server, o.getString(FIELD_UUID));
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();


        }
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);


    }


}

