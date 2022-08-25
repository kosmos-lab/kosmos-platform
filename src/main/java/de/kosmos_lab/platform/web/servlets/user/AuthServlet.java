package de.kosmos_lab.platform.web.servlets.user;

import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.annotations.tags.Tag;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public AuthServlet(KosmoSWebServer webServer, IController controller) {
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), description = "Login successful", content = @Content(mediaType = "application/jwt", schema = @Schema(type = SchemaType.STRING, example = "eyJ0eXBlIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJsZXZlbCI6MSwibmFtZSI6ImphbiIsImV4cCI6MTYwMzQ1NDE4NDY1NSwiaGFzaCI6Ii0ifQ.gAQh1snnG_VlzJ-lv4X7_-A0GV7iQA_l83b1285mPSo"))),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), description = "The credentials did not match"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),


            }
    )
    @Override
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
 throws ParameterNotFoundException{
        IUser u = controller.tryLogin(request.getString(FIELD_USER), request.getString(FIELD_PASS));
        if (u != null) {
            try {
                sendJWT(request, response, controller.getJwt().sign(u.toJWT()));
                return;
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                //e.printStackTrace();
                logger.warn("error while taking auth", e);
            }
        }
        response.setStatus(403);


    }

}

