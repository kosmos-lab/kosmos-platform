package de.kosmos_lab.platform.web.servlets.server;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.http.HttpServletResponse;

@ApiEndpoint(
        path = "/server/stop",
        userLevel = 100
)
public class ServerStopServlet extends KosmoSAuthedServlet {
    
    
    public ServerStopServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
        
    }
    @Operation(
            tags = {"server"},
            summary = "stop",
            description = "stop the server",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "Server will be shut down"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {
        
        (new Thread(this.controller::stop)).start();
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
        return;
        
    }
    
    
}

