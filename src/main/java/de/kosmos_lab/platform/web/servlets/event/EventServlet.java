package de.kosmos_lab.platform.web.servlets.event;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Event;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.io.IOException;

;


@ApiEndpoint(
        path = "/event",
        userLevel = 1
)
public class EventServlet extends KosmoSAuthedServlet {


    public EventServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"event"},
            summary = "event",
            description = "send an event",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(

                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(
                                            ref = "#/components/schemas/event"
                                    )

                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NO_RESPONSE), description = "event received"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws IOException, DeviceNotFoundException, ParameterNotFoundException, NoAccessToScope, UnauthorizedException {


        JSONObject o = request.getBodyAsJSONObject();
        if (o != null) {


            this.controller.fireEvent(new Event(this.controller, this.server, o, null), this.server);
            response.setStatus(WebServer.STATUS_NO_RESPONSE);
            return;





        }



    }


}

