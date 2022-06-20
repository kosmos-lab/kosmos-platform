package de.kosmos_lab.kosmos.exceptions;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String user) {
        super("Cannot find the User "+user);
    }
}
