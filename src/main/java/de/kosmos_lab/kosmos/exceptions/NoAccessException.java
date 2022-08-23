package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "The request could not be processed, because you have no access to do this.\nSee errormessage for details")
public class NoAccessException extends Exception {

    public NoAccessException(String message) {
        super(message);
    }
}
