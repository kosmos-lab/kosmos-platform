package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.data.Group;

public class NoAccessToGroup extends NoAccessException {
    public NoAccessToGroup(Group group) {
        super("No Access to group "+group.getName());
    }
}
