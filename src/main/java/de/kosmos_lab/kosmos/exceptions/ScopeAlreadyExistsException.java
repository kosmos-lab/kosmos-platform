package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;

public class ScopeAlreadyExistsException extends AlreadyExistsException {
    
    
    public ScopeAlreadyExistsException(String name) {
        super("Scope with the name "+name+" already exists");
        
    }
}
