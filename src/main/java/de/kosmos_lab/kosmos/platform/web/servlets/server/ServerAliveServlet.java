package de.kosmos_lab.kosmos.platform.web.servlets.server;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(
        path = "/server/alive"
)
public class ServerAliveServlet extends KosmoSServlet {


    public ServerAliveServlet(WebServer webServer, IController controller) {
        super(webServer, controller);

    }
    @Operation(
            tags = {"server"},
            summary = "alive",
            description = "check if the server is alive",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK), description = "Server is reachable and running"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) {
        try {
            sendText(request, response, "online");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}

