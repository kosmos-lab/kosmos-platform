package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "The request could not be processed, because you have no access to do this.\nSee errormessage for details")
public class NoAccessException extends Exception {

    public NoAccessException(String message) {
        super(message);
    }
}
