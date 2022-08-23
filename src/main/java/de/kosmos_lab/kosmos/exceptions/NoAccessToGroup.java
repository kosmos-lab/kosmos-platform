package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_FORBIDDEN), description = "You don't have access to this group.")
public class NoAccessToGroup extends NoAccessException {
    public NoAccessToGroup(Group group) {
        super("No access to group "+group.getName());
    }
}
