package de.kosmos_lab.kosmos.exceptions;

public class ScopeNotFoundException extends NotFoundException {
    public ScopeNotFoundException(String scope) {
        super("Cannot find the Scope "+scope);
    }
}
