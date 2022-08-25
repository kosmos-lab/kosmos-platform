package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.exceptions.ServletException;

public class NotFoundException extends ServletException {
    public NotFoundException(String message) {
        super(message);
    }
}
