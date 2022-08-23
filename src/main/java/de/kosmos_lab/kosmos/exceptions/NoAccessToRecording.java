package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "You don't have access to this recording.")
public class NoAccessToRecording extends NoAccessException {
    public NoAccessToRecording() {
        super("You dont have access to this recording");
    }
}
