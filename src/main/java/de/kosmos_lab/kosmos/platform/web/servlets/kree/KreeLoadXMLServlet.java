package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.Schema;
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

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/kree/loadXML",
        userLevel = 1

)
public class KreeLoadXMLServlet extends AuthedServlet {


    public KreeLoadXMLServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }

    @Operation(
            tags = {"kree"},
            summary = "load xml",
            description = "Loads the block xml back from persistence",
            
            responses = {
                    @ApiResponse(
                            description = "XML Block definition",

                            responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK),
                            content = {
                                    @Content(
                                            mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_XML)

                            }

                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        String xml = this.server.getRulesService().getXML(request.getKosmoSUser());
        if (xml != null) {
            response.setStatus(STATUS_OK);
            sendXML(request, response, xml);
        } else {
            sendXML(request, response, "<xml xmlns=\"https://developers.google.com/blockly/xml\">\n</xml>");
        }
        
    
    }


}

