package de.kosmos_lab.kosmos.platform.smarthome;

import de.kosmos_lab.kosmos.data.Device;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface CommandInterface extends CommandSourceName {
    
    void deviceAdded(@CheckForNull CommandInterface from, @Nonnull Device device,@Nonnull CommandSourceName source);
    
    void deviceRemoved(@CheckForNull CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source);
    
    void deviceUpdate(@CheckForNull CommandInterface from, @Nonnull Device device, @CheckForNull String key, @Nonnull CommandSourceName source);
    
    void stop();
}
