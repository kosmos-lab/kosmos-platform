package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), description = "The request could not be processed, because you have no access to do this.\nSee errormessage for details")
public class NoAccessException extends Exception {

    public NoAccessException(String message) {
        super(message);
    }
}
