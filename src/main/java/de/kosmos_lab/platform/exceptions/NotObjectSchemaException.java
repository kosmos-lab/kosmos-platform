package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.exceptions.ServletException;

public class NotObjectSchemaException extends ServletException {

    public NotObjectSchemaException() {
        super("The given input is no ObjectSchema");
    }
}
