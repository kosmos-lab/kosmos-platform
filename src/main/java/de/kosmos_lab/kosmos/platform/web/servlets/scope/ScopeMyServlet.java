package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.ArraySchema;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.annotations.tags.Tag;
import de.kosmos_lab.kosmos.data.Scope;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/scope/my",
        userLevel = 1
)

@Tag(description = "Scope handling",name="scope")
public class ScopeMyServlet extends AuthedServlet {





    public ScopeMyServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    private static final String FIELD_USER = "user";
    private static final String FIELD_ADMIN = "admin";
    @Operation(
            tags = {"scope"},
            summary = "my",
            description = "Lists the scopes you have access to",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
                            description = "A JSONObject stating the scopes you have access to",
                            content = @Content(

                            mediaType = MediaType.APPLICATION_JSON,
                            schemaProperties = {
                                    @SchemaProperty(
                                            name = FIELD_ADMIN,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(type = SchemaType.STRING, description = "The names of the scopes you have admin access to")
                                            )

                                    ),
                                    @SchemaProperty(
                                            name = FIELD_USER,
                                            array = @ArraySchema(
                                                    uniqueItems = true,
                                                    arraySchema = @Schema(type = SchemaType.STRING, description = "The names of the scopes you have user access to")
                                            )
                                    )
                            })),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Scope s : this.controller.getAllScopesWithUser(request.getKosmoSUser())) {
            jsonArray.put(s.getName());
        }
        jsonObject.put(FIELD_USER, jsonArray);
        jsonArray = new JSONArray();
        for (Scope s : this.controller.getAllScopesWithAdmin(request.getKosmoSUser())) {
            jsonArray.put(s.getName());
        }
        jsonObject.put(FIELD_ADMIN,jsonArray);
        sendJSON(request, response, jsonObject);
    }
}
