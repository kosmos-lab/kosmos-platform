package de.kosmos_lab.kosmos.platform.web.servlets.openapi;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.OpenApiParser;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(path ="/doc/openapi.json", userLevel = -1)
public class OpenApiJSONServlet extends KosmoSServlet {
    public String cached = null;

    public OpenApiJSONServlet(WebServer webServer, IController controller) {
        super(webServer, controller);

    }
    @Operation(
            tags = {"OpenApi"},
            summary = "openapi.json",
            description = "The generated openApi specification for this service in JSONObject format",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), description = "The generated openApi specification for this service"),
            }
    )
    @Override
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParameterNotFoundException {
        if (cached == null) {


            cached = OpenApiParser.getJSON().toString(2);

        }
        sendText(request, response, cached);


    }

}

