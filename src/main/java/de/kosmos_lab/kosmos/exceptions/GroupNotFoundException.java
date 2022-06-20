package de.kosmos_lab.kosmos.exceptions;

public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(String group) {
        super("Cannot find the Group "+group);
    }
    public GroupNotFoundException(int group) {
        super("Cannot find the Group "+group);
    }
}
