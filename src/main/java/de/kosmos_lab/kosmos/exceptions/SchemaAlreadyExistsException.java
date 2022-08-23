package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_CONFLICT), description = "There is already a schema with that $id.")
public class SchemaAlreadyExistsException extends AlreadyExistsException {
    
    
    public SchemaAlreadyExistsException(String id) {
        super("Schema with $id "+id+" already exists");
        
    }
}
