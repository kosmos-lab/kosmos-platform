package de.dfki.baall.helper.webserver.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_MISSING_VALUE), description = "The request could not be processed, are all required properties/parameters filled?\nSee errormessage for details")
public class ParameterNotFoundException extends Exception {
    public ParameterNotFoundException(String key) {
        super("could not find parameter " + key);
    }
}
