package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Scope;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), description = "You don't have access to this scope.")

public class NoAccessToScope extends NoAccessException {
    public NoAccessToScope(Scope scope) {
        super("No access to scope "+scope.getName());
    }
}
