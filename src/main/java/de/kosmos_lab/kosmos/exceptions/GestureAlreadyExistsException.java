package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;


public class GestureAlreadyExistsException extends AlreadyExistsException {


    public GestureAlreadyExistsException(String name) {
        super("Gesture with name "+name+" already exists");

    }
}
