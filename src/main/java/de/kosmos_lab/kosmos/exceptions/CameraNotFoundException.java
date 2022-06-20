package de.kosmos_lab.kosmos.exceptions;

public class CameraNotFoundException extends NotFoundException {
    public CameraNotFoundException(String uuid) {
        super("Cannot find the Camera "+uuid);
    }
}
