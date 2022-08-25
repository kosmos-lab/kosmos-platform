package de.kosmos_lab.platform.web.servlets.schreibtrainer;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;


@Tag(
        name = "schreibtrainer",
        description = "Schreibtrainer"
)
@ApiEndpoint(
        path = "/schreibtrainer/add",
        userLevel = 1
)

public class SchreibtrainerAddServlet extends KosmoSAuthedServlet {


    public SchreibtrainerAddServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                                                    name = SchreibtrainerConstants.FIELD_UUID,
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The pen was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws NotObjectSchemaException, ParameterNotFoundException {


        JSONObject o = request.getBodyAsJSONObject();
        try {
            if (o.has(SchreibtrainerConstants.FIELD_UUID)) {
                SchreibtrainerConstants.getDevice(controller, server, o.getString(SchreibtrainerConstants.FIELD_UUID));
                response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();


        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_ERROR);


    }


}

