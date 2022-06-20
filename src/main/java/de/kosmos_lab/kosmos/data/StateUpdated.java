package de.kosmos_lab.kosmos.data;


import javax.annotation.Nonnull;

public class StateUpdated {
    public final Device device;
    public final String key;
    public final long lastUpdated;
    
    public StateUpdated(@Nonnull Device device, @Nonnull String key, long lastUpdated) {
        this.device = device;
        this.key = key;
        this.lastUpdated = lastUpdated;
    }
}
