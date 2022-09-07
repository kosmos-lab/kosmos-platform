package de.kosmos_lab.platform.smarthome;

import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Event;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface EventInterface  {
    
    void eventFired(@CheckForNull EventInterface from, @Nonnull Event event);
    

    
    void stop();
}
