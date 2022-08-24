package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given group.")
public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(String group) {
        super("Cannot find the group with name "+group);
    }
    public GroupNotFoundException(int group) {
        super("Cannot find the group with id "+group);
    }
}
