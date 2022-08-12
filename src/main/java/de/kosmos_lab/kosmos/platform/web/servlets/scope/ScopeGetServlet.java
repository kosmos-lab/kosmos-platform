package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.ArraySchema;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ApiResponseDescription;
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
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/scope/get",
        userLevel = 1
)
@ApiResponse(
        componentName="scopeGet",
        responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE),
        description = "Details about the scope",

        content = @Content(

                mediaType = MediaType.APPLICATION_JSON,
                examples = {
                        @ExampleObject(
                                name="testScope",
                                value="{\""+Scope.FIELD_NAME+"\":\"testScope\",\""+Scope.FIELD_ID+"\":18,\""+Scope.FIELD_USERS+"\":[{\"name\":\"user7\",\"id\":7}],\""+Scope.FIELD_ADMINS+"\":[{\"name\":\"admin\",\"id\":1}],\""+Scope.FIELD_ADMIN_GROUPS+"\":[],\""+Scope.FIELD_USER_GROUPS+"\":[{\"name\":\"testGroup2\",\"id\":2}]}"
                        )
                },
                schemaProperties = {
                        @SchemaProperty(
                                name = Scope.FIELD_NAME,
                                schema = @Schema(type = SchemaType.STRING, description = "The name of the scope")
                        ),
                        @SchemaProperty(
                                name = Scope.FIELD_ID,
                                schema = @Schema(type = SchemaType.INTEGER, description = "The ID of the scope")
                        ),
                        @SchemaProperty(
                                name = Scope.FIELD_ADMINS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of users with admin level access to the scope",
                                                ref = "#/components/schemas/userNameID"
                                        )
                                )
                        ),
                        @SchemaProperty(
                                name = Scope.FIELD_USERS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of users with user level access to the scope",
                                                ref = "#/components/schemas/userNameID"
                                        )
                                )
                        ),
                        @SchemaProperty(
                                name = Scope.FIELD_USER_GROUPS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of groups with user level access to the scope",
                                                ref = "#/components/schemas/groupNameID"
                                        )
                                )
                        ),
                        @SchemaProperty(

                                name = Scope.FIELD_ADMIN_GROUPS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of groups with admin level access to the scope",
                                                ref = "#/components/schemas/groupNameID"
                                        )
                                )
                        )
                }))
public class ScopeGetServlet extends AuthedServlet {
    public final static String FIELD_NAME = "name";
    public final static String FIELD_ID = "id";

    public ScopeGetServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"scope"},
            summary = "get",
            description = "get information about the scope",
            parameters = {
                    @Parameter(
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK),ref = "#/components/responses/scopeGet"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ScopeNotFoundException, ParameterNotFoundException {




        try {
            String sname = request.getString(FIELD_NAME);

            try {
                Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
                sendJSON(request, response, scope.toJSON());
                return;
            } catch (NotFoundInPersistenceException ex) {
                throw new ScopeNotFoundException(sname);
            }


        } catch (ParameterNotFoundException ex) {

        }
        int id = request.getInt(FIELD_ID);
        try {
            Scope scope = controller.getScope(id, CacheMode.CACHE_AND_PERSISTENCE);
            sendJSON(request, response, scope.toJSON());
            return;
        } catch (NotFoundInPersistenceException ex) {
            throw new ScopeNotFoundException("" + id);
        }


    }


}

