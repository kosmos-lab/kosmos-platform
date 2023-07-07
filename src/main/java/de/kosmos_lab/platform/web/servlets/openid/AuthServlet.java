package de.kosmos_lab.platform.web.servlets.openid;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;


@ApiEndpoint(
        path = AuthServlet.path
)
public class AuthServlet extends KosmoSServlet {
    public static final String path = "/openid/auth";

    public AuthServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws UnauthorizedException {
        String host = request.getParameter("host");
        if (host == null) {
            try {
                host = request.getRequest().getHeader("host");
            } catch (Exception ex) {

            }
        }
        String uri = server.getOpenIDLink(host, "/openid/callback", null);
        if (uri != null) {
            response.setStatus(302);
            response.setHeader("Location", uri);
            return;
        }
        response.setStatus(WebServer.STATUS_NOT_FOUND);


    }

}