package de.kosmos_lab.kosmos.platform.web.servlets.server;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.annotation.WebServlet;
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "Server will be shut down"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) {
        
        (new Thread(this.controller::stop)).start();
        response.setStatus(STATUS_NO_RESPONSE);
        return;
        
    }
    
    
}

