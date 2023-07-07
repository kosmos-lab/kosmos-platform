package de.kosmos_lab.platform.data;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class StateUpdates {
    public HashMap<String, Long> map = new HashMap<>();

    public void addUpdate(@Nonnull String key, long age) {
        this.map.put(key, age);
    }

}
