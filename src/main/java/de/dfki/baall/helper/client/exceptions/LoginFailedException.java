package de.dfki.baall.helper.client.exceptions;

public class LoginFailedException extends RequestFailedException {
    
    public LoginFailedException() {
        super("Login to server failed");
    }
}
