package de.kosmos_lab.kosmos.exceptions;

public class NoAccessToRecording extends NoAccessException {
    public NoAccessToRecording() {
        super("You dont have access to this recording");
    }
}
