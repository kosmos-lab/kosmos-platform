package de.kosmos_lab.platform.web.servlets.openid;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;


@ApiEndpoint(
        path = AuthWebServlet.path
)
public class AuthWebServlet extends KosmoSServlet {
    public static final String path = "/openid/auth/web";

    public AuthWebServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller);
    }

    @Operation(
            tags = {"openid"},
            description = "Used to login to an openId provider, if one is given",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "redirectTo", required = false, schema = @Schema(type = SchemaType.STRING)
                            , description = "the URI the user is returned to after the auth was successful, the callback will be done with a parameter 'token' which contains a valid JWT for the current session")},

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_OK), description = "redirect to openId provider if setup"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "there is no openId provider defined"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws UnauthorizedException {
        String host = request.getParameter("host");
        if (host == null) {
            try {
                host = request.getRequest().getHeader("host");
            } catch (Exception ex) {

            }
        }
        String uri = server.getOpenIDLink(host, "/openid/callback/web", request.getString("redirectTo", null));
        if (uri != null) {
            response.setStatus(302);
            response.setHeader("Location", uri);
            return;
        }
        response.setStatus(WebServer.STATUS_NOT_FOUND);


    }

}