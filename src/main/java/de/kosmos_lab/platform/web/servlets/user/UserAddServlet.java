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
import de.kosmos_lab.platform.data.KosmoSUser;
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
        path = "/user/add",
        userLevel = 100
)

public class UserAddServlet extends KosmoSAuthedServlet {

    private static final String FIELD_USER = "user";
    private static final String FIELD_PASS = "pass";
    private static final String FIELD_LEVEL = "level";

    public UserAddServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    public UserAddServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller, 100);

    }

    @Operation(
            tags = {"user"},
            summary = "add",
            description = "Add a user to the system.",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_USER,
                                                    schema = @Schema(
                                                            description = "The username of the new user.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_PASS,
                                                    schema = @Schema(
                                                            description = "The password of the new user.",
                                                            type = SchemaType.STRING,
                                                            minLength = 6,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_LEVEL,
                                                    schema = @Schema(
                                                            description = "The level of the newly added user - must be less then your own level.",
                                                            type = SchemaType.INTEGER,
                                                            minimum = "1",
                                                            defaultValue = "1",
                                                            required = false
                                                    )
                                            )
                                    },
                                    examples = {
                                            @ExampleObject(
                                                    name = "add user testUser with level 1",
                                                    value = "{\"" + FIELD_USER + "\":\"testUser\",\"" + FIELD_PASS + "\":\"ksajfbaf\",\"" + FIELD_LEVEL + "\":1}"
                                            ),
                                            @ExampleObject(
                                                    name = "add user testUser2 with level 10",
                                                    value = "{\"" + FIELD_USER + "\":\"testUser2\",\"" + FIELD_PASS + "\":\"ksajfbaf\",\"" + FIELD_LEVEL + "\":10}"
                                            ),
                                            @ExampleObject(
                                                    name = "add user testUser2 with level 1",
                                                    value = "{\"" + FIELD_USER + "\":\"testUser3\",\"" + FIELD_PASS + "\":\"ksajfbaf\"}"
                                            )
                                    }
                            )
                    }),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "OK - user was added"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "Conflict - The user already exists"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_ERROR), description = "#/components/responses/FailedError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, ParameterNotFoundException {
        String user = request.getString(FIELD_USER);
        if (user.length() < 3) {
            response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE, "username is not long enough (minLength is 3)");
            return;
        }
        String pass = request.getString(FIELD_PASS);
        if (pass.length() < 6) {
            response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE, "password is not long enough (minLength is 6)");
            return;
        }
        int level = request.getInt(FIELD_LEVEL, 1);
        logger.info("trying to add user {} with level {}", user, level);
        IUser me = request.getKosmoSUser();
        if (level <=0) {
            response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE, "The level is too low, minimum is 1");
        }
        if (level >= me.getLevel()) {
            response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN, String.format("You are not allowed to create a user with this level - maximum is %d", me.getLevel() - 1));
            return;
        }
        logger.info("going to add user {} with level {}", user, level);

        IUser u = this.controller.getUser(user);
        if (u == null) {
            logger.info("user is not yet taken {} with level {}", user, level);
            String salt = StringFunctions.generateRandomKey();
            String hash = controller.getPasswordHash(pass, salt);
            u = new KosmoSUser(controller, user, 0, level, hash, salt);
            controller.addUser(u);
            logger.info("finished adding user {} with level {}", user, level);
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
            return;
        }
        response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT, "This user already exists!");


    }


}

