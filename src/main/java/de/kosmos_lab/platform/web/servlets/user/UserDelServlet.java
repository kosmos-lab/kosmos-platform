package de.kosmos_lab.platform.web.servlets.user;

import de.kosmos_lab.platform.exceptions.NoAccessException;
import de.kosmos_lab.platform.exceptions.UserNotFoundException;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;


@ApiEndpoint(
        path = "/user/delete",
        userLevel = 100
)
public class UserDelServlet extends KosmoSAuthedServlet {
    private static final String FIELD_USER = "user";

    public UserDelServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    @Operation(

            tags = {"user"},
            summary = "delete",
            description = "This endpoint can be used to delete a user from the system.",
            parameters = {
                    @Parameter(
                            name = FIELD_USER,
                            in = ParameterIn.QUERY,
                            schema = @Schema(
                                    description = "The username of the user to delete",
                                    type = SchemaType.STRING,
                                    minLength = 3,
                                    required = true
                            )
                    )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "OK - user was deleted"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), description = "The user could not be found"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_ERROR), description = "#/components/responses/FailedError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ParameterNotFoundException, UnauthorizedException, UserNotFoundException, NoAccessException {
        String user = request.getString(FIELD_USER);
        IUser u = controller.getUser(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }

        IUser me = request.getKosmoSUser();
        if (u.getLevel() >= me.getLevel()) {
            throw new NoAccessException("Your level is not high enough to add a user with that level");
        }
        controller.deleteUser(u);
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);


    }


}

