package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.http.HttpServletResponse;


@ApiEndpoint(
        path = "/user/delete",
        userLevel = 100
)
public class UserDelServlet extends AuthedServlet {
    private static final String FIELD_USER = "user";

    public UserDelServlet(WebServer webServer, IController controller, int level) {
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "OK - user was deleted"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND), description = "The user could not be found"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR), description = "#/components/responses/FailedError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ParameterNotFoundException {
        String user = request.getString(FIELD_USER);
        IUser u = controller.getUser(user);
        if (u == null) {
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND);
            return;
        }

        IUser me = request.getKosmoSUser();
        if (u.getLevel() >= me.getLevel()) {
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
            return;
        }
        controller.deleteUser(u);
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);


    }


}

