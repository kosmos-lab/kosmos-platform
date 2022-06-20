package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;

public class DeviceAlreadyExistsException extends AlreadyExistsException {
    
    
    public DeviceAlreadyExistsException(String uuid) {
        super("Device with uuid "+uuid+" already exists");
        
    }
}
