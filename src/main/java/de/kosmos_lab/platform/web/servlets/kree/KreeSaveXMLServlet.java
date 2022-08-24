package de.kosmos_lab.platform.web.servlets.kree;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/kree/saveXML",
        userLevel = 1

)
public class KreeSaveXMLServlet extends KosmoSAuthedServlet {
    public KreeSaveXMLServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE),description = "XML saved successfully" ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE),description = "Could not save XML, the request body was empty." ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        String xml = request.getBody();
        if (xml.length() > 0) {
            server.getRulesService().saveXML(request.getKosmoSUser(), xml);
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);

            return;
        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE);
        return;
    }


}

