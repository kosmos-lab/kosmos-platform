package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.ArraySchema;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@WebServlet(urlPatterns = {"/group/my"}, loadOnStartup = 1)
public class GroupMyServlet extends AuthedServlet {
    
    
    public GroupMyServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    private static final String FIELD_USER = "user";
    private static final String FIELD_ADMIN = "admin";
    @Operation(
            tags = {"scope"},
            summary = "my",
            description = "Lists the scopes you have access to",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK),
                            description = "A JSONObject stating the groups you have access to",
                            content = @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_ADMIN,
                                                    array = @ArraySchema(
                                                            uniqueItems = true,
                                                            arraySchema = @Schema(type = SchemaType.STRING, description = "The names of the groups you have admin access to")
                                                    )

                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    array = @ArraySchema(
                                                            uniqueItems = true,
                                                            arraySchema = @Schema(type = SchemaType.STRING, description = "The names of the groups you have user access to")
                                                    )
                                            )
                                    })),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope {
        JSONObject o = new JSONObject();
    
        JSONArray arr = new JSONArray();
        for (Group group : this.controller.getAllGroupsWithUser(request.getKosmoSUser())) {
        
            arr.put(group.getName());
        
        }
        o.put(FIELD_USER,arr);
        arr = new JSONArray();
        for (Group group : this.controller.getAllGroupsWithAdmin(request.getKosmoSUser())) {
        
            arr.put(group.getName());
        
        }
        o.put(FIELD_ADMIN,arr);
        sendJSON(request, response, o);
    }
    
    
}

