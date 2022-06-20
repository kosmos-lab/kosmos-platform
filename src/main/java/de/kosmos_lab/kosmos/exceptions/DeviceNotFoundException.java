package de.kosmos_lab.kosmos.exceptions;

public class DeviceNotFoundException extends NotFoundException {
    public DeviceNotFoundException(String uuid) {
        super("Cannot find the Device "+uuid);
    }
}
