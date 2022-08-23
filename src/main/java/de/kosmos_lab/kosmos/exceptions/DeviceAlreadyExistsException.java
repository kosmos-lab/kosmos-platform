package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;


@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_CONFLICT), description = "There is already a device with that uuid.")
public class DeviceAlreadyExistsException extends AlreadyExistsException {
    
    
    public DeviceAlreadyExistsException(String uuid) {
        super("Device with uuid "+uuid+" already exists");
        
    }
}
