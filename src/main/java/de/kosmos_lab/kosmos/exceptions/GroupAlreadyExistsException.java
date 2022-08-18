package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a group with that name.")
public class GroupAlreadyExistsException extends AlreadyExistsException {
    
    
    public GroupAlreadyExistsException(String uuid) {
        super("Group with name "+uuid+" already exists");
        
    }
}
