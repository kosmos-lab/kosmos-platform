package de.kosmos_lab.platform.web.servlets.scope;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.NotFoundException;
import de.kosmos_lab.platform.exceptions.ScopeNotFoundException;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
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
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

@ApiEndpoint(
        path = "/scope/deluser",
        userLevel = 1
)

public class ScopeDelUserServlet extends KosmoSAuthedServlet {


    public ScopeDelUserServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    private static final String FIELD_USER = "user";
    private static final String FIELD_SCOPE = "scope";

    @Operation(
            tags = {"scope"},
            summary = "delete user",
            description = "delete a users access from a scope",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_SCOPE,
                                                    schema = @Schema(
                                                            description = "The scope from which to remove the user",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    schema = @Schema(
                                                            description = "The username of the user to delete from the scope",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),

                                    },
                                    examples = {
                                            @ExampleObject(
                                                    name = "add user 'testUser' to scope 'testScope'",
                                                    value = "{\"" + FIELD_SCOPE + "\":\"testScope\",\"" + FIELD_USER + "\":\"testUser\"}"
                                            ),
                                    }
                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The user was removed from the group successfully."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws NotFoundException, NoAccessToScope, ParameterNotFoundException, UnauthorizedException {

        String sname = request.getString(FIELD_SCOPE);
        String uname = request.getString(FIELD_USER);
        try {
            Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);

            if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                IUser u = controller.getUser(uname);
                if (u != null) {
                    if (scope.hasUser(u)) {
                        controller.delScopeUser(scope, u);

                    }
                    response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
                throw new UserNotFoundException(uname);
            } else {
                throw new NoAccessToScope(scope);
            }
        } catch (NotFoundInPersistenceException ex) {
            throw new ScopeNotFoundException(sname);
        }

    }


}

