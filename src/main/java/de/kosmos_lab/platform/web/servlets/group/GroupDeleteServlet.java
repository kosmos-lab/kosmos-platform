package de.kosmos_lab.platform.web.servlets.group;

import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NoAccessToGroup;
import de.kosmos_lab.platform.exceptions.NotFoundException;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(path = "/group/delete", userLevel = 1)
public class GroupDeleteServlet extends KosmoSAuthedServlet {


    private static final String FIELD_GROUP = "group";

    public GroupDeleteServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    @Operation(
            tags = {"group"},
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The group was deleted successfully."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),

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
                response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                return;
            }
            throw new NoAccessToGroup(group);
            

        
    }
    
    
}

