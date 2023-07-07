package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.server.WebServer;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given camera.")
public class CameraNotFoundException extends NotFoundException {
    public CameraNotFoundException(String uuid) {
        super("Cannot find the camera " + uuid);
    }
}
