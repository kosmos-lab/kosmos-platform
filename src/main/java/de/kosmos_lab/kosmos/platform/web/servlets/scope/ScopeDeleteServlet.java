package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Scope;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.ScopeNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/scope/delete",
        userLevel = 1
)

public class ScopeDeleteServlet extends AuthedServlet {


    public final static String FIELD_NAME = "name";
    public final static String FIELD_ID = "id";

    public ScopeDeleteServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"scope"},
            summary = "delete",
            description = "deletes a scope",
            //delete cannot have a body, so we need to declare it with parameters
            parameters = {
                    @Parameter(
                            description = "The name of the scope to delete. Use either 'name' or 'id'.",
                            in = ParameterIn.QUERY,
                            name = FIELD_NAME,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            )
                    ),
                    @Parameter(
                            description = "The ID of the scope to delete. Use either 'name' or 'id'.",
                            in = ParameterIn.QUERY,
                            name = FIELD_ID,
                            schema = @Schema(
                                    type = SchemaType.INTEGER
                            )
                    ),

            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The scope was deleted successfully."),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),

            }
    )
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, ScopeNotFoundException, ParameterNotFoundException {

        try {
            String sname = request.getString(FIELD_NAME);

            try {
                Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {

                    controller.deleteScope(scope);
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                return;


            } catch (NotFoundInPersistenceException ex) {
                throw new ScopeNotFoundException(sname);
            }


        } catch (ParameterNotFoundException ex) {

        }
        try {
            int id = request.getInt(FIELD_ID);
            try {

                Scope scope = controller.getScope(id, CacheMode.CACHE_AND_PERSISTENCE);
                if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {

                    controller.deleteScope(scope);
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                return;


            } catch (NotFoundInPersistenceException ex) {
                throw new ScopeNotFoundException("" + id);
            }

        } catch (ParameterNotFoundException ex) {
            ex.printStackTrace();
        }


        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE);


    }


}

