package de.kosmos_lab.kosmos.platform.web.servlets.user;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.annotations.tags.Tag;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@ApiEndpoint(
        path = "/user/login"
)
@Tag(
        name = "user",
        description = "User handling"
)
public class AuthServlet extends KosmoSServlet {
    private static final String FIELD_USER = "user";
    private static final String FIELD_PASS = "pass";

    public AuthServlet(WebServer webServer, IController controller) {
        super(webServer, controller);

    }

    @Operation(

            tags = {"user"},
            summary = "authorize",
            description = "Used to get a JWT token from the system.\n" +
                    "This token should be included as a header (Authorization) for all other requests.",
            parameters = {
                    @Parameter(
                            name = FIELD_USER,
                            description = "the username of the user",
                            schema = @Schema(
                                    type = SchemaType.STRING
                            ),
                            in = ParameterIn.QUERY,
                            required = true,
                            example = "karl"
                    ),
                    @Parameter(
                            name = FIELD_PASS,
                            description = "the password of the user",
                            schema = @Schema(
                                    type = SchemaType.STRING
                            ),
                            in = ParameterIn.QUERY,
                            required = true,
                            examples = {@ExampleObject(value = "test")}
                    )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK), description = "Login successful", content = @Content(mediaType = "application/jwt", schema = @Schema(type = SchemaType.STRING, example = "eyJ0eXBlIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJsZXZlbCI6MSwibmFtZSI6ImphbiIsImV4cCI6MTYwMzQ1NDE4NDY1NSwiaGFzaCI6Ii0ifQ.gAQh1snnG_VlzJ-lv4X7_-A0GV7iQA_l83b1285mPSo"))),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), description = "The credentials did not match"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),


            }
    )
    @Override
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParameterNotFoundException {
        IUser u = controller.tryLogin(request.getString(FIELD_USER), request.getString(FIELD_PASS));
        if (u != null) {
            try {
                sendJWT(request, response, controller.getJwt().sign(u.toJWT()));
                return;
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                //e.printStackTrace();
                logger.warn("error while taking auth", e);
            }
        }
        response.setStatus(403);


    }

}

