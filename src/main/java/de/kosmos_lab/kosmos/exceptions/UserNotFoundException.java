package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "Could not find the given user.")
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String user) {
        super("Cannot find the user "+user);
    }
}
