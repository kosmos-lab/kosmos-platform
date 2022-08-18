package de.kosmos_lab.kosmos.exceptions;


import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;

@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "The schema could not be found, please verify that the $id is valid and a reachable URL.")
public class SchemaNotFoundException extends NotFoundException {
    public SchemaNotFoundException(String schemaName) {
        super("Could not find schema "+schemaName);
    }
}
