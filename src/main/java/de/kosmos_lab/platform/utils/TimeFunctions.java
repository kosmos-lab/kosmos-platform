package de.kosmos_lab.platform.utils;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class TimeFunctions {


    /**
     * Converts the given milliseconds to a SRT Timestamp
     *
     * @param milliseconds the amount of milliseconds to convert to an SRT Timestamp
     *
     * @return
     */
    @Nonnull
    public static String createSRTTimestamp(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        long ms = milliseconds % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);

    }
}
