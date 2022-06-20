package de.kosmos_lab.kosmos.data;

import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.Date;

public class LogEntry {
    
    private final Date date;
    private final JSONObject state;
    private final String uuid;
    private final String source;
    
    public LogEntry(@Nonnull String uuid, long date,@Nonnull  String source,@Nonnull  JSONObject json) {
        this.uuid = uuid;
        this.date = new Date(date);
        this.source = source;
        this.state = json;
    }
    
    public @Nonnull JSONObject getState() {
        return this.state;
    }
    
    public @Nonnull Date getDate() {
        return date;
    }
    
    public @Nonnull String getSource() {
        return source;
    }
    
    public @Nonnull String getUuid() {
        return this.uuid;
    }
}
