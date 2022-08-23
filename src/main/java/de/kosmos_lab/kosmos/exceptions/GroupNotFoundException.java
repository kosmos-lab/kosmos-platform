package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given group.")
public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(String group) {
        super("Cannot find the group with name "+group);
    }
    public GroupNotFoundException(int group) {
        super("Cannot find the group with id "+group);
    }
}
