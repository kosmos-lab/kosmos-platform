package de.kosmos_lab.kosmos.exceptions;

import de.dfki.baall.helper.webserver.WebServer;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given device.")
public class DeviceNotFoundException extends NotFoundException {
    public DeviceNotFoundException(String uuid) {
        super("Cannot find the device "+uuid);
    }
}
