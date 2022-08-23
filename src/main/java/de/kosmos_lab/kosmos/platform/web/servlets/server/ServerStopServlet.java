package de.kosmos_lab.kosmos.platform.web.servlets.server;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.http.HttpServletResponse;

@ApiEndpoint(
        path = "/server/stop",
        userLevel = 100
)
public class ServerStopServlet extends AuthedServlet {
    
    
    public ServerStopServlet(WebServer webServer, IController controller,int level) {
        super(webServer, controller, level);
        
    }
    @Operation(
            tags = {"server"},
            summary = "stop",
            description = "stop the server",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "Server will be shut down"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {
        
        (new Thread(this.controller::stop)).start();
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
        return;
        
    }
    
    
}

