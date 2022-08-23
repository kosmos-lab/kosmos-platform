package de.kosmos_lab.kosmos.exceptions;


import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "The schema could not be found, please verify that the $id is valid and a reachable URL.")
public class SchemaNotFoundException extends NotFoundException {
    public SchemaNotFoundException(String schemaName) {
        super("Could not find schema "+schemaName);

    }
}
