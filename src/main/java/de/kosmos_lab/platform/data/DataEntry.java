package de.kosmos_lab.platform.data;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * used to hold a generic JSON-Schema Driven Object
 *
 * extends JSONObject to have simple access to all of the properties
 */
public class DataEntry extends JSONObject {
    
    private final DataSchema schema;
    
    /**
     * create a new DateEntry object from a schema and a json
     *
     * @param schema
     * @param json
     * @throws ValidationException
     */
    public DataEntry(@Nonnull DataSchema schema, @Nonnull JSONObject json,boolean force) throws ValidationException {
        super();
        //silentOverwrite(json);
        if ( !force) {
            schema.validate(json);
        }
        silentOverwrite(json);
        this.schema = schema;
    }
    
    /**
     * DONT USE THIS UNLESS YOU KNOW WHAT YOU ARE DOING!!
     * set/overwrites all keys with the values of the given json
     * does NOT validate
     *
     * @param json
     */
    protected void silentOverwrite(@Nonnull JSONObject json) {
        //thats just stupid... but we need to copy all entries without overwriting the JSONObject itself (which is our parent class)
        for (String key : json.keySet()) {
            put(key, json.get(key));
        }
    }
    
    /**
     * creates a new DataEntry from a schema and an json string
     *
     * @param schema
     * @param json
     * @throws ValidationException
     */
    public DataEntry(@Nonnull DataSchema schema, @Nonnull String json) throws ValidationException {
        super(json);
        schema.validate(this);
        this.schema = schema;
    }
    
    /**
     * gets the DataSchema
     *
     * @return
     */
    public DataSchema getDataSchema() {
        return this.schema;
    }
    
    /**
     * gets the JSON-Schema
     *
     * @return the Schema
     */
    public ObjectSchema getSchema() {
        return this.schema.getSchema();
    }
    
}
