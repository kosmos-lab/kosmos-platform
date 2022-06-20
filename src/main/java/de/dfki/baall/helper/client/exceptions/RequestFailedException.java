package de.dfki.baall.helper.client.exceptions;

public class RequestFailedException extends Exception {
    
    public RequestFailedException(Exception ex) {
        super(ex);
    }
    
    
    public RequestFailedException(String text) {
        super(text);
        
    }
    
    
}