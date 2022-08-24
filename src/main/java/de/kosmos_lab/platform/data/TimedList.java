package de.kosmos_lab.platform.data;

import org.json.JSONArray;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

class TimedListEntry<T> {
    long validTo;
    T value;
    
    public TimedListEntry(@Nonnull T entry, long validTo) {
        this.value = entry;
        this.validTo = validTo;
        
    }
}

public class TimedList {
    private final long timeout;
    private final LinkedList<TimedListEntry> entries;
    
    
    public TimedList(long timeout) {
        this.timeout = timeout;
        this.entries = new LinkedList<>();
    }
    
    public void addEntry(@Nonnull Object entry) {
        addEntry(entry, System.currentTimeMillis());
    }
    
    private void addEntry(@Nonnull Object entry, long time) {
        this.entries.add(new TimedListEntry(entry, time + timeout));
    }
    
    public void clear() {
        this.entries.clear();
    }

    @Nonnull public LinkedList<TimedListEntry> getEntries() {
        long now = System.currentTimeMillis();
        LinkedList values = new LinkedList();
        Iterator<TimedListEntry> itr = this.entries.iterator();
        while (itr.hasNext()) {
            TimedListEntry e = itr.next();
            if (e.validTo < now) {
                itr.remove();
            } else {
                values.add(e.value);
            }
            
        }
        //could use this to cleanup, but we want to extract the values anyway, so just use the good old iterator for it
        //this.entries.removeIf(e -> (e.validTo<now));
        
        return values;
        
    }

    @Nonnull public JSONArray toJSONArray() {
        JSONArray arr = new JSONArray();
        for (Object e : getEntries()) {
            arr.put(e);
        }
        return arr;
    }

    @Nonnull public Collection<String> toStringList() {
        LinkedList<String> list = new LinkedList<String>();
        for (Object e : getEntries()) {
            list.add(e.toString());
        }
        return list;
    }

    @Nonnull public String toString() {
        
        return toJSONArray().toString();
    }
}
