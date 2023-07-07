package de.kosmos_lab.platform.web.servlets.schema;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;

@ApiEndpoint(
        path = "/schema/delete",
        userLevel = 1

)
public class SchemaDeleteServlet extends KosmoSAuthedServlet {


    public SchemaDeleteServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"schema"},
            summary = "delete",
            description = "Deletes the schema given by the id",
            parameters = {@Parameter(in = ParameterIn.QUERY, name = "id", schema = @Schema(type = SchemaType.STRING), description = "the $id of the schema (most likely its an URI)")},

            responses = {

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), description = "The schema was not found"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The schema was deleted"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "The schema cannot be deleted - it is still in use!"),

                    // @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws SchemaNotFoundException, ParameterNotFoundException, UnauthorizedException {
        String id = request.getString("id");
        DataSchema s = controller.getSchema(id);

        if (s != null) {
            if (this.controller.getPersistence().getNumberOfDevicesWithSchema(s) == 0) {

                this.controller.deleteSchema(s);
                response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT);
            return;
        }


        throw new SchemaNotFoundException(id);


    }


}

