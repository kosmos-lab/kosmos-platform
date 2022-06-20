package de.kosmos_lab.kosmos.exceptions;

import de.kosmos_lab.kosmos.data.Scope;

public class NoAccessToScope extends NoAccessException {
    public NoAccessToScope(Scope scope) {
        super("No Access to scope "+scope.getName());
    }
}
