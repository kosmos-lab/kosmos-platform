package de.kosmos_lab.platform.exceptions;

import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.server.WebServer;


@ApiResponse(responseCode = @ResponseCode(statusCode = WebServer.STATUS_CONFLICT), description = "There is already a device with that uuid.")
public class DeviceAlreadyExistsException extends AlreadyExistsException {


    public DeviceAlreadyExistsException(String uuid) {
        super("Device with uuid " + uuid + " already exists");

    }
}
