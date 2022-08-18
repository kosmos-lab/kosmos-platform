package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a gesture with those exact same points.")
public class GestureAlreadyExistsException extends AlreadyExistsException {


    public GestureAlreadyExistsException(String name) {
        super("Gesture with name "+name+" already exists");

    }
}
