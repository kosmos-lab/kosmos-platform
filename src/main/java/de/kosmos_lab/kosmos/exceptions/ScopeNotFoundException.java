package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "Could not find the given scope.")
public class ScopeNotFoundException extends NotFoundException {
    public ScopeNotFoundException(String scope) {
        super("Cannot find the scope "+scope);
    }
}
