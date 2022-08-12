package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.ScopeNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/scope/addadmin",
        userLevel = 1
)
public class ScopeAddAdminServlet extends AuthedServlet {

    private static final String FIELD_USER = "user";
    private static final String FIELD_SCOPE = "scope";
    public ScopeAddAdminServlet(WebServer webServer, IController controller,int level) {
        super(webServer, controller,level);
    }
    @Operation(
            tags = {"scope"},
            summary = "add admin",
            description = "add a new admin to a scope",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_SCOPE,
                                                    schema = @Schema(
                                                            description = "The scope to which to add the user to",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    schema = @Schema(
                                                            description = "The username of the user to add to the scope",
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The user was added successfully"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ScopeNotFoundException, ParameterNotFoundException {
        String sname = request.getString(FIELD_SCOPE);
        String uname = request.getString(FIELD_USER);
        try {
            Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
            
            if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                IUser u = controller.getUser(uname);
                if (u != null) {
                    controller.addScopeAdmin(scope, u);
                    response.setStatus(STATUS_NO_RESPONSE);
                    return;
                }
                
            } else {
                throw new NoAccessToScope(scope);
            }
        } catch (NotFoundInPersistenceException ex) {
            throw new ScopeNotFoundException(sname);
        }
        
        
    }
    
    
}

