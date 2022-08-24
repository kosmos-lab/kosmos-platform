package de.kosmos_lab.platform.camera.subtitles.data;

import javax.annotation.Nonnull;

public class SubtitleEntry {


    @Nonnull public String getUuid() {
        return uuid;
    }
    
    private final String uuid;
    
    public long getFrom() {
        return from;
    }
    
    public long getTo() {
        return to;
    }

    @Nonnull public String getText() {
        return text;
    }
    
    public long from;
    public long to;
    public String text;
    
    public SubtitleEntry(long from, long to, @Nonnull String text,@Nonnull  String uuid) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.uuid = uuid;
    }
}
