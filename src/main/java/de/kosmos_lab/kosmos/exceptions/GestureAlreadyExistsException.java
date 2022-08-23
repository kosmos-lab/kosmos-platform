package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_CONFLICT), description = "There is already a gesture with those exact same points.")
public class GestureAlreadyExistsException extends AlreadyExistsException {


    public GestureAlreadyExistsException(String name) {
        super("Gesture with name "+name+" already exists");

    }
}
