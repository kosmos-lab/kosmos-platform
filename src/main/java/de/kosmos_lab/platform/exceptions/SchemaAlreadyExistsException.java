package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_CONFLICT), description = "There is already a schema with that $id.")
public class SchemaAlreadyExistsException extends AlreadyExistsException {
    
    
    public SchemaAlreadyExistsException(String id) {
        super("Schema with $id "+id+" already exists");
        
    }
}
