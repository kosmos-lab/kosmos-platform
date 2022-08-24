package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.server.WebServer;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;

@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_NOT_FOUND), description = "Could not find the given device.")
public class DeviceNotFoundException extends NotFoundException {
    public DeviceNotFoundException(String uuid) {
        super("Cannot find the device "+uuid);
    }
}
