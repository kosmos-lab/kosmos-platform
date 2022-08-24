package de.kosmos_lab.platform.exceptions;


import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "The schema could not be found, please verify that the $id is valid and a reachable URL.")
public class SchemaNotFoundException extends NotFoundException {
    public SchemaNotFoundException(String schemaName) {
        super("Could not find schema "+schemaName);

    }
}
