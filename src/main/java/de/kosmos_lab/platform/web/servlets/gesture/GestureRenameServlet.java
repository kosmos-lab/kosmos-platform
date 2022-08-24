package de.kosmos_lab.platform.web.servlets.gesture;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.GestureNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/gesture/rename",
        userLevel = 1
)
public class GestureRenameServlet extends KosmoSAuthedServlet {
    
    
    public GestureRenameServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"gesture"},
            summary = "rename",
            description = "Renames all instances of a gesture to a new name",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {

                                            @SchemaProperty(
                                                    name = "from",
                                                    schema = @Schema(
                                                            description = "The old name of this gesture",
                                                            type = SchemaType.STRING,
                                                            minLength = 1,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "to",
                                                    schema = @Schema(
                                                            description = "The new name to use for this gesture",
                                                            type = SchemaType.STRING,
                                                            minLength = 1,
                                                            required = true
                                                    )
                                            )
                                    }, examples = {
                                    @ExampleObject(
                                            name = "from 'T' to 'capitalT'",
                                            value = "{\"from\":\"T:\",\"to\":\"capitalT\"}"
                                    ),

                            }

                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), description = "The gesture was added successfully", ref = "#/components/responses/gestureList"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "This gestures was not found"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, ParameterNotFoundException, GestureNotFoundException {
        String from = request.getString("from");
        String to = request.getString("to");
        if (controller.getGestureProvider().renameGesture(from,to)) {
            sendJSON(request, response, GestureListServlet.getGestureList(this.controller));
            return;
        }
        throw new GestureNotFoundException(from);
        
    }
    
}

