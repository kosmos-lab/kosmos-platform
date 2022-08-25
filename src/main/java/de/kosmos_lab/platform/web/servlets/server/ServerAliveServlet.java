package de.kosmos_lab.platform.web.servlets.server;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(
        path = "/server/alive"
)
public class ServerAliveServlet extends KosmoSServlet {


    public ServerAliveServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller);

    }
    @Operation(
            tags = {"server"},
            summary = "alive",
            description = "check if the server is alive",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), description = "Server is reachable and running"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
 {
        try {
            sendText(request, response, "online");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}

