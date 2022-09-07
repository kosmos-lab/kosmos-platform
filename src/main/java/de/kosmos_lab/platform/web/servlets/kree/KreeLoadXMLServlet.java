package de.kosmos_lab.platform.web.servlets.kree;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ApiEndpoint(
        path = "/kree/loadXML",
        userLevel = 1

)
public class KreeLoadXMLServlet extends KosmoSAuthedServlet {


    public KreeLoadXMLServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    @Operation(
            tags = {"kree"},
            summary = "load xml",
            description = "Loads the block xml back from persistence",
            
            responses = {
                    @ApiResponse(
                            description = "XML Block definition",

                            responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_XML)

                            }

                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException , UnauthorizedException {
        String xml = this.server.getRulesService().getXML(request.getKosmoSUser());
        if (xml != null) {
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_OK);
            sendXML(request, response, xml);
        } else {
            sendXML(request, response, "<xml xmlns=\"https://developers.google.com/blockly/xml\">\n</xml>");
        }
        
    
    }


}

