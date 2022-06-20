package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;

public class SchemaAlreadyExistsException extends AlreadyExistsException {
    
    
    public SchemaAlreadyExistsException(String id) {
        super("Schema with $id "+id+" already exists");
        
    }
}
