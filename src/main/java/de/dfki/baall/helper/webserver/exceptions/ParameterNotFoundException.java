package de.dfki.baall.helper.webserver.exceptions;

public class ParameterNotFoundException extends Exception {
    public ParameterNotFoundException(String key) {
        super("could not find parameter " + key);
    }
}
