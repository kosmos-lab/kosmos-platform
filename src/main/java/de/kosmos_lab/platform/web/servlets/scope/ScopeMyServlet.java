package de.kosmos_lab.platform.web.servlets.scope;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/scope/my",
        userLevel = 1
)

@Tag(description = "Scope handling",name="scope")
public class ScopeMyServlet extends KosmoSAuthedServlet {





    public ScopeMyServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    private static final String FIELD_USER = "user";
    private static final String FIELD_ADMIN = "admin";
    @Operation(
            tags = {"scope"},
            summary = "my",
            description = "Lists the scopes you have access to",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
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
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, UnauthorizedException {
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
