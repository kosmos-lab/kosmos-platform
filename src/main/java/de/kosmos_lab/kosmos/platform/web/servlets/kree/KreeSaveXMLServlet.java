package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/kree/saveXML",
        userLevel = 1

)
public class KreeSaveXMLServlet extends AuthedServlet {
    public KreeSaveXMLServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    @Operation(
            tags = {"kree"},
            summary = "save xml",
            description = "Saves the block xml to persistence",
            requestBody = @RequestBody(
                    description = "the XML of the blocks",
                    content = {
                            @Content(
                                    mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_XML)
                    }

            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE),description = "XML saved successfully" ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE),description = "Could not save XML, the request body was empty." ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        String xml = request.getBody();
        if (xml.length() > 0) {
            server.getRulesService().saveXML(request.getKosmoSUser(), xml);
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);

            return;
        }
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
        return;
    }


}

