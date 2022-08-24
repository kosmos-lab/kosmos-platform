package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "You don't have access to this scope.")

public class NoAccessToScope extends NoAccessException {
    public NoAccessToScope(Scope scope) {
        super("No access to scope "+scope.getName());

    }
}
