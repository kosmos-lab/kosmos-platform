package de.dfki.baall.helper.webserver.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), description = "The request could not be processed, are all required properties/parameters filled?\nSee errormessage for details")
public class ParameterNotFoundException extends Exception {
    public ParameterNotFoundException(String key) {
        super("could not find parameter " + key);
    }
}
