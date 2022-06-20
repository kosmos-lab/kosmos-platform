package de.kosmos_lab.kosmos.data;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class LoggingRequest {
    
    private final Set<String> properties;
    private final String uuid;
    
    public LoggingRequest(@Nonnull String uuid) {
        this(uuid, new HashSet<>());
    }
    
    public LoggingRequest(@Nonnull String uuid,@Nonnull  Set<String> properties) {
        this.uuid = uuid;
        this.properties = properties;
    }
    
    public LoggingRequest(@Nonnull String uuid, @Nonnull String property) {
        this (uuid,new HashSet<>());
        this.addProperty(property);
    }
    
    public LoggingRequest(@Nonnull String uuid, @Nonnull String[] props) {
        this (uuid,new HashSet<>());
        for (String prop : props) {
            this.addProperty(prop);
        }
    }
    
    public void addProperty(@Nonnull String property) {
        properties.add(property);
    }
    
    public @Nonnull Set<String> getProperties() {
        return properties;
    }
    
    public @Nonnull String getUuid() {
        return uuid;
    }
}
