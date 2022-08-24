package de.kosmos_lab.platform.utils;

import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class LocalSchemaClient implements SchemaClient {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("LocalSchemaClient");
    
    private final SchemaClient fallbackClient;
    private final String namespace;
    
    public LocalSchemaClient(String url) {
        this.fallbackClient = new DefaultSchemaClient();
        this.namespace = url;
    }
    
    @Override
    public InputStream get(String url) {
        url = url.replace(".json.json",".json");
        logger.trace("getting {}",url);
        if (url.startsWith(namespace)) {
            String nurl = url.substring(namespace.length());
            File f = new File(nurl);
            if (f.exists()) {
                //System.out.println("found it locally");
                try {
                    return new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return fallbackClient.get(url);
    }
    
    
}