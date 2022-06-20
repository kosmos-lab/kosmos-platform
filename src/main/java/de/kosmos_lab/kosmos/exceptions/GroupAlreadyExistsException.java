package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;

public class GroupAlreadyExistsException extends AlreadyExistsException {
    
    
    public GroupAlreadyExistsException(String uuid) {
        super("Group with name "+uuid+" already exists");
        
    }
}
