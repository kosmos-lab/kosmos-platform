package de.kosmos_lab.platform.web.servlets.group;

import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NoAccessToGroup;
import de.kosmos_lab.platform.exceptions.NotFoundException;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(path = "/group/deluser", userLevel = 1)
public class GroupDelUserServlet extends KosmoSAuthedServlet {
    
    
    public GroupDelUserServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    public final static String FIELD_GROUP = "group";
    public final static String FIELD_USER = "user";
    @Operation(
            tags = {"group"},
            summary = "delete user",
            description = "delete a user from a group",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_GROUP,
                                                    schema = @Schema(
                                                            description = "The group from which to remove the user",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    schema = @Schema(
                                                            description = "The username of the user to delete from the group",
                                                            type = SchemaType.STRING,

                                                            required = true
                                                    )
                                            ),

                                    },
                                    examples = {
                                            @ExampleObject(
                                                    name = "add user 'testUser' to scope 'testGroup'",
                                                    value = "{\"" + FIELD_GROUP + "\":\"testGroup\",\"" + FIELD_USER + "\":\"testUser\"}"
                                            ),
                                    }
                            )
                    }
            ),

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The user was removed from the group successfully."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            
            throws ServletException, IOException, NotObjectSchemaException, NotFoundInPersistenceException, NoAccessToGroup, NotFoundException, ParameterNotFoundException {
        
        String sname = request.getString(FIELD_GROUP);
        String uname = request.getString(FIELD_USER);
            Group group = controller.getGroup(sname, CacheMode.CACHE_AND_PERSISTENCE);
            
            if (group.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                IUser u = controller.getUser(uname);
                if (u != null) {
                    controller.delGroupUser(group, u);
                    response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
                throw new UserNotFoundException(uname);
                
            } else {
                throw new NoAccessToGroup(group);
            }


        
    }
    
    
}

