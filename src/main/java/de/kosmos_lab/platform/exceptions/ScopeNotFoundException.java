package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.server.WebServer;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given scope.")
public class ScopeNotFoundException extends NotFoundException {
    public ScopeNotFoundException(String scope) {
        super("Cannot find the scope " + scope);

    }
}
