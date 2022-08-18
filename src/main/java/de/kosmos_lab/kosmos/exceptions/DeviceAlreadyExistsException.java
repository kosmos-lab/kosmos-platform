package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;


@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a device with that uuid.")
public class DeviceAlreadyExistsException extends AlreadyExistsException {
    
    
    public DeviceAlreadyExistsException(String uuid) {
        super("Device with uuid "+uuid+" already exists");
        
    }
}
