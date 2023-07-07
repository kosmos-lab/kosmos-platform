package de.kosmos_lab.platform.web.servlets.openid;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


@ApiEndpoint(
        path = CallbackServlet.path
)
public class CallbackServlet extends KosmoSServlet {
    public static final String path = "/openid/callback";

    public CallbackServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller);
    }


    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws UnauthorizedException, ParameterNotFoundException {
        UUID uuid = UUID.fromString(request.getString("state"));
        IUser u = server.processKC(request.getString("code"), uuid);
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