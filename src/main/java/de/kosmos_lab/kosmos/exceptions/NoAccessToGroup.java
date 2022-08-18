package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), description = "You don't have access to this group.")
public class NoAccessToGroup extends NoAccessException {
    public NoAccessToGroup(Group group) {
        super("No access to group "+group.getName());
    }
}
