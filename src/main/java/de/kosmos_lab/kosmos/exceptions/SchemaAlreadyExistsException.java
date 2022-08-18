package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a schema with that $id.")
public class SchemaAlreadyExistsException extends AlreadyExistsException {
    
    
    public SchemaAlreadyExistsException(String id) {
        super("Schema with $id "+id+" already exists");
        
    }
}
