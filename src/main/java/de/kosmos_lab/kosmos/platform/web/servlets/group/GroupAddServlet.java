package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GroupAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@WebServlet(urlPatterns = {"/group/add"}, loadOnStartup = 1)
public class GroupAddServlet extends AuthedServlet {
    
    public final static String FIELD_NAME = "name";
    
    public GroupAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    @Operation(
            tags = {"group"},
            summary = "add",
            description = "Create a new group",
            requestBody = @RequestBody(
                    
                    required = true,
                    content = {
                            
                            @Content(
                                    examples = {
                                            @ExampleObject(
                                                    name = "add group with name 'testGroup'",
                                                    value = "{\"" + FIELD_NAME + "\":\"testGroup\"}"
                                            ),
                                        
                                    },
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_NAME,
                                                    schema = @Schema(
                                                            description = "name of the new group.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    
                                                    )
                                            
                                            ),
                                        
                                    }
                            
                            )
                    }),
            responses = {
                    
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), ref = "#/components/responses/scopeGet", description = "The group was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a group with that name."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
                
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, ParameterNotFoundException, GroupAlreadyExistsException {
        String group_name = request.getString(FIELD_NAME);
        if (group_name.length() < 3) {
            response.sendError(de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE, "group name is not long enough (minLength is 3)");
            return;
        }
        
        
        controller.addGroup(group_name, request.getKosmoSUser());
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
        return;
        
    }
    
}

