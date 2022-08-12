package de.dfki.baall.helper.persistence.exceptions;

public abstract class AlreadyExistsException extends Exception {
    
    
    public AlreadyExistsException(String message) {
        super(message);
    }
}
