package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GestureNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/gesture/delete",
        userLevel = 1
)
public class GestureDeleteServlet extends AuthedServlet {
    
    
    public GestureDeleteServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "delete",
            description = "Delete a gesture from the system.",
            parameters = {@Parameter(name = "id",
                    in = ParameterIn.QUERY,
                    schema = @Schema(
                            description = "The uuid of the device to delete",
                            type = SchemaType.STRING,
                            minLength = 3,
                            required = true
                    )
            )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), description = "The gesture was added successfully", ref = "#/components/responses/gestureList"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "This gestures was not found"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, ParameterNotFoundException, GestureNotFoundException {
        String id = request.getString("id");
        if (controller.getGestureProvider().deleteGesture(id)) {
            sendJSON(request, response, GestureListServlet.getGestureList(this.controller));
            return;
        }
        throw new GestureNotFoundException(id);
        
    }
    
}

