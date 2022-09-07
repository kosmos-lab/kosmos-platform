package de.kosmos_lab.platform.web.servlets.scope;

import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.GroupNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.exceptions.ScopeAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/scope/add",
        userLevel = 1
)
public class ScopeAddServlet extends KosmoSAuthedServlet {


    public ScopeAddServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"scope"},
            summary = "add",
            description = "add a new scope",
            requestBody = @RequestBody(

                    required = true,
                    content = {

                            @Content(
                                    examples = {
                                            @ExampleObject(
                                                    name="add scope with name 'testScope'",
                                                    value="{\""+Scope.FIELD_NAME+"\":\"testScope\"}"
                                            ),
                                            @ExampleObject(
                                                    name="add scope with name 'testScope2', and add user 7 as user and user 2 as admin",
                                                    value="{\""+Scope.FIELD_NAME+"\":\"testScope2\",\""+Scope.FIELD_USERS+"\":[7],\""+Scope.FIELD_ADMINS+"\":[2]}"
                                            ),
                                            @ExampleObject(
                                                    name="add scope with name 'testScope2', and add user 7 as user and user 2 as admin",
                                                    value="{\""+Scope.FIELD_NAME+"\":\"testScope2\",\""+Scope.FIELD_USERS+"\":[7],\""+Scope.FIELD_ADMINS+"\":[2]}"
                                            )
                                    },
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = Scope.FIELD_NAME,
                                                    schema = @Schema(
                                                            description = "name of the new scope.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true

                                                    )

                                            ),
                                            @SchemaProperty(

                                                    name = Scope.FIELD_USERS,
                                                    array = @ArraySchema(

                                                            schema = @Schema(
                                                                    description = "an optional list of user names and/or user IDs to add as user to the new scope.",
                                                                    oneOfRef = {
                                                                            "#/components/schemas/userID",
                                                                            "#/components/schemas/userName"
                                                                    })

                                                    )
                                            ),
                                            @SchemaProperty(

                                                    name = Scope.FIELD_ADMINS,
                                                    array = @ArraySchema(

                                                            schema = @Schema(
                                                                    description = "an optional list of user names and/or user IDs to add as admin to the new scope.",
                                                                    oneOfRef = {
                                                                            "#/components/schemas/userID",
                                                                            "#/components/schemas/userName"
                                                                    })

                                                    )
                                            ),
                                            @SchemaProperty(

                                                    name = Scope.FIELD_USER_GROUPS,
                                                    array = @ArraySchema(

                                                            schema = @Schema(
                                                                    description = "an optional list of user names and/or group IDs to add as admin to the new scope.",
                                                                    oneOfRef = {
                                                                            "#/components/schemas/groupID",
                                                                            "#/components/schemas/groupName"
                                                                    })

                                                    )
                                            ),
                                            @SchemaProperty(

                                                    name = Scope.FIELD_ADMIN_GROUPS,
                                                    array = @ArraySchema(

                                                            schema = @Schema(
                                                                    description = "an optional list of user names and/or group IDs to add as admin to the new scope.",
                                                                    oneOfRef = {
                                                                            "#/components/schemas/groupID",
                                                                            "#/components/schemas/groupName"
                                                                    })

                                                    )
                                            ),
                                    }

                            )
                    }),
            responses = {

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),ref = "#/components/responses/scopeGet",description = "The scope was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "There is already a scope with that name."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),

            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws NoAccessToScope, ParameterNotFoundException, AlreadyExistsException, GroupNotFoundException, UnauthorizedException, IOException {
        String sname = request.getString(Scope.FIELD_NAME);

        try {
            Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
            throw new ScopeAlreadyExistsException(sname);

        } catch (NotFoundInPersistenceException e) {
            //e.printStackTrace();
            Scope scope = controller.addScope(sname, request.getKosmoSUser());

            try {
                JSONArray users = request.getJSONArray(Scope.FIELD_USERS);
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        Object utoken = users.get(i);

                        if (utoken instanceof String) {
                            IUser u = controller.getUser(users.getString(i));
                            if (u != null) {
                                scope.addUser(u);
                            }
                        }
                        if (utoken instanceof Integer) {
                            try {
                                scope.addUser(controller.getUser(users.getInt(i)));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (utoken instanceof JSONObject) {

                            try {
                                scope.addUser(controller.getUser(users.getJSONObject(i).getInt("id")));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (ParameterNotFoundException ex) {
                ex.printStackTrace();

            }
            try {
                JSONArray users = request.getJSONArray(Scope.FIELD_ADMINS);
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {
                            IUser u = controller.getUser(users.getString(i));
                            if (u != null) {
                                scope.addAdmin(u);
                            }
                        }
                        if (utoken instanceof Integer) {
                            try {
                                scope.addAdmin(controller.getUser(users.getInt(i)));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (utoken instanceof JSONObject) {

                            try {
                                scope.addAdmin(controller.getUser(users.getJSONObject(i).getInt("id")));
                            } catch (UserNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (ParameterNotFoundException ex) {
                ex.printStackTrace();

            }
            try {
                JSONArray users = request.getJSONArray(Scope.FIELD_ADMIN_GROUPS);
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {

                            Group g = controller.getGroup(users.getString(i), CacheMode.CACHE_AND_PERSISTENCE);
                            if (g != null) {
                                scope.addAdminGroup(g);
                            }


                        }
                        if (utoken instanceof Integer) {


                            scope.addAdminGroup(controller.getGroup(users.getInt(i), CacheMode.CACHE_AND_PERSISTENCE));


                        }
                        if (utoken instanceof JSONObject) {


                            scope.addAdminGroup(controller.getGroup(users.getJSONObject(i).getInt("id"), CacheMode.CACHE_AND_PERSISTENCE));

                        }
                    }
                }
            } catch (ParameterNotFoundException ex) {
                ex.printStackTrace();

            }
            try {
                JSONArray users = request.getJSONArray(Scope.FIELD_USER_GROUPS);
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        Object utoken = users.get(i);
                        if (utoken instanceof String) {

                            Group g = controller.getGroup(users.getString(i), CacheMode.CACHE_AND_PERSISTENCE);

                            scope.addUserGroup(g);


                        }
                        if (utoken instanceof Integer) {


                            scope.addUserGroup(controller.getGroup(users.getInt(i), CacheMode.CACHE_AND_PERSISTENCE));


                        }
                        if (utoken instanceof JSONObject) {


                            scope.addUserGroup(controller.getGroup(users.getJSONObject(i).getInt("id"), CacheMode.CACHE_AND_PERSISTENCE));

                        }
                    }
                }
            } catch (ParameterNotFoundException ex) {
                ex.printStackTrace();

            }
            sendJSON(request, response, scope.toJSON());

            //response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_OK);


            return;
        }


    }


}

