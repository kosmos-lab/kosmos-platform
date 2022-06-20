package de.kosmos_lab.kosmos.platform.utils;


import de.kosmos_lab.kosmos.data.DataSchema;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Comparator;

@SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
public class SchemaComparator implements Comparator<DataSchema> {
    
    @Override
    public int compare(DataSchema o1, DataSchema o2) {
        
        return (o1.getSchema()).getId().compareTo((o2.getSchema()).getId());
        
    }
    
}