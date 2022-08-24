package de.kosmos_lab.platform.web.servlets.group;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/group/my",
        userLevel = 1
)

public class GroupMyServlet extends KosmoSAuthedServlet {
    
    
    public GroupMyServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    private static final String FIELD_USER = "user";
    private static final String FIELD_ADMIN = "admin";
    @Operation(
            tags = {"group"},
            summary = "my",
            description = "Lists the scopes you have access to",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
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
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
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

