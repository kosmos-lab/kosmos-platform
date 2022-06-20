package de.kosmos_lab.kosmos.exceptions;

public class SchemaNotFoundException extends NotFoundException {
    public SchemaNotFoundException(String schemaName) {
        super("Could not find Schema "+schemaName);
    }
}
