package de.kosmos_lab.platform.web.servlets.openid;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import java.io.IOException;
import java.util.UUID;


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
        String host = request.getParameter("host");;
        if ( host == null ) {
            try {
                host = request.getRequest().getHeader("host");
            } catch (Exception ex) {

            }
        }
        String uri = server.getOpenIDLink(host,"/openid/callback",null);
        if ( uri != null ) {
            response.setStatus(302);
            response.setHeader("Location", uri);
            return;
        }
        response.setStatus(WebServer.STATUS_NOT_FOUND);



    }

}