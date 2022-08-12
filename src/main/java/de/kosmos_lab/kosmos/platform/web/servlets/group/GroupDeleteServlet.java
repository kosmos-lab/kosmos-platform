package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToGroup;
import de.kosmos_lab.kosmos.exceptions.NotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/group/delete"}, loadOnStartup = 1)
public class GroupDeleteServlet extends AuthedServlet {


    private static final String FIELD_GROUP = "group";

    public GroupDeleteServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    @Operation(
            tags = {"scope"},
            summary = "delete",
            description = "Delete a Group from the system.",
            //delete cannot have a body, so we need to declare it with parameters
            parameters = {
                    @Parameter(
                            description = "The name of the scope to delete. Use either 'name' or 'id'.",
                            in = ParameterIn.QUERY,
                            name = FIELD_GROUP,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            ), required = true
                    )

            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The group was deleted successfully."),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),

            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, NotFoundInPersistenceException, NoAccessToGroup, NotFoundException, ParameterNotFoundException {
        //logger.info("starting into group/delete");
        //
        String groupname = request.getString(FIELD_GROUP);
        
            Group group = controller.getGroup(groupname, CacheMode.CACHE_AND_PERSISTENCE);
            //logger.info("found the group");
            if (group.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
                //logger.info("user is allowed to delete");
                controller.delGroup(group);
                response.setStatus(STATUS_NO_RESPONSE);
                return;
            }
            throw new NoAccessToGroup(group);
            

        
    }
    
    
}

