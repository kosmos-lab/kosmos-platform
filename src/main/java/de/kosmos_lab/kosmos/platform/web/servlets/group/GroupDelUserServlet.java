package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToGroup;
import de.kosmos_lab.kosmos.exceptions.NotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.UserNotFoundException;
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

@WebServlet(urlPatterns = {"/group/deluser"}, loadOnStartup = 1)
public class GroupDelUserServlet extends AuthedServlet {
    
    
    public GroupDelUserServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }

    public final static String FIELD_GROUP = "group";
    public final static String FIELD_USER = "user";
    @Operation(
            tags = {"scope"},
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The user was removed from the group successfully."),
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
                    response.setStatus(STATUS_NO_RESPONSE);
                    return;
                }
                throw new UserNotFoundException(uname);
                
            } else {
                throw new NoAccessToGroup(group);
            }


        
    }
    
    
}

