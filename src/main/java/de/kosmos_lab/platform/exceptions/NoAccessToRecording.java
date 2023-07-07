package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.server.WebServer;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "You don't have access to this recording.")
public class NoAccessToRecording extends NoAccessException {
    public NoAccessToRecording() {
        super("You dont have access to this recording");
    }
}
