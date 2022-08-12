package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
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
            description = "saves the block xml to persistence",
            requestBody = @RequestBody(
                    description = "the XML of the blocks",
                    content = {
                            @Content(

                                    mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_XML)

                    }

            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE),description = "XML saved successfully" ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        String xml = request.getBody();
        if (xml.length() > 0) {
            server.getRulesService().saveXML(request.getKosmoSUser(), xml);
            response.setStatus(STATUS_NO_RESPONSE);

            return;
        }
        response.setStatus(STATUS_FAILED);
        return;
    }


}

