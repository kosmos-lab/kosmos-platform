package de.kosmos_lab.platform.web.servlets.user;

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
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.utils.StringFunctions;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/user/password",
        userLevel = 1
)
public class UserPasswordServlet extends KosmoSAuthedServlet {
    private static final String FIELD_USER = "user";
    private static final String FIELD_PASS = "pass";

    public UserPasswordServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    @Operation(

            tags = {"user"},
            summary = "update password",
            description = "update the password of a user",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    schema = @Schema(
                                                            description = "The username of the new user, if the username is empty your own password will be changed",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = false

                                                    )

                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_PASS,
                                                    schema = @Schema(
                                                            description = "The password of the new user",
                                                            type = SchemaType.STRING,
                                                            minLength = 6,
                                                            required = true

                                                    )
                                            ),

                                    },
                                    examples = {
                                            @ExampleObject(
                                                    name = "Set own password to 'asdasdasd'",
                                                    value = "{\"" + FIELD_PASS + "\":\"asdasdasd\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "Set password of user 'testUser3' to 'ksajfbaf'",
                                                    value = "{\"" + FIELD_USER + "\":\"testUser3\",\"" + FIELD_PASS + "\":\"ksajfbaf\"}"
                                            )
                                    }
                            )
                    }),

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "OK - password was changed"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), description = "The user was not found"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, ParameterNotFoundException {

        IUser u = null;
        try {
            String user = request.getString(FIELD_USER);
            u = this.controller.getUser(user);
        } catch (ParameterNotFoundException ex) {
            //ignore exception and set user to ourselves
            u = request.getKosmoSUser();
        }

        String pass = request.getString(FIELD_PASS);
        if (u != null) {
            if (this.isMeOrAmAdmin(request, u)) {
                String salt = StringFunctions.generateRandomKey();
                String hash = controller.getPasswordHash(pass, salt);
                controller.setUserPassword(u, salt, hash);
                response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN);
            return;
        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND);
        return;


    }


}

