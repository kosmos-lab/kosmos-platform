package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Scope;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "You don't have access to this scope.")

public class NoAccessToScope extends NoAccessException {
    public NoAccessToScope(Scope scope) {
        super("No access to scope "+scope.getName());

    }
}
