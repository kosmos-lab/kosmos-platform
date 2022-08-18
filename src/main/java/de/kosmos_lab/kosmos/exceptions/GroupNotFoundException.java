package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "Could not find the given group.")
public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(String group) {
        super("Cannot find the group with name "+group);
    }
    public GroupNotFoundException(int group) {
        super("Cannot find the group with id "+group);
    }
}
