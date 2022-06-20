package de.kosmos_lab.kosmos.platform.experiments;


import de.kosmos_lab.kosmos.platform.KosmoSController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.RunMode;

import java.io.File;

public class Experiments {
    
    public static void main(String[] args) throws InterruptedException {
    
    
        KosmoSController c = new KosmoSController(new File("config/config.json"), RunMode.TEST);
        //c.publishUpdate(null,null,null,null);
        System.err.println(c.getClass().getPackage().getImplementationVersion());
        //IPersistence p = c.getPersistence();
        /*if (p instanceof SQLPersistence) {
            System.out.println(((SQLPersistence) p).createSQL("device_location"));
        }*/
        System.exit(0);
        
       
        
        /*File dir = new File("schema");
        
        
        JSONObject properties = new JSONObject();
        JSONObject exam = new JSONObject();
        JSONArray ex = new JSONArray();
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".example.json") && f.getName().endsWith(".json")) {
                try {
                    JSONObject json = new JSONObject(new JSONTokener(new FileReader(f)));
                    SchemaLoader loader = SchemaLoader.builder()
                            .schemaClient(new LocalSchemaClient("https://kosmos-lab.de/"))
                            .schemaJson(json)
                            //.resolutionScope("classpath://schema/") // setting the default resolution scope
                            .build();
    
                    Schema s = loader.load().build();
                    if (s instanceof ObjectSchema) {
                        continue;
                    }
                    json.remove("$id");
                    json.remove("$schema");
                    properties.put(json.getString("title"), json);
                    exam.put(json.getString("title"),DataSchema.getExample(s));
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        ex.put(exam);
        
        JSONObject s = new JSONObject();
        s.put("examples",ex);
        s.put("$schema","http://json-schema.org/draft-07/schema#");
        s.put("$id","https://https://kosmos-lab.de/schema/All.json");
        s.put("properties",properties);
        s.put("additionalProperties",false);
        s.put("title","All");
        s.put("type","object");
        
        
        KosmosSFileWriter.writeToFile(new File("schema/All.json"),s.toString(4));
        
        if (true) System.exit(0);
        */
        
        //System.out.println(c.createSQL("owlmatches"));
       /* Controller c = new KosmoSController();
        IPersistence p = c.getPersistence();
        if (p instanceof SQLPersistence) {
            System.out.println(((SQLPersistence) p).createSQL("states"));
        }
        */
        /*
        Controller c = new KosmoSController(new File("config.json"),true);
        IPersistence p = c.getPersistence();
        if (p instanceof SQLPersistence) {
            System.out.println(((SQLPersistence) p).createSQL("scope_devices"));
        }
        System.exit(0);
        
         */
    /*
        try {
            KosmoSClient client = new KosmoSClient("http://localhost:18080","admin","pass");
            System.out.println(client.getJSONArray("/device/list"));
            System.out.println(client.postJSONObject("/device/set",new JSONObject("{\"id\":\"multi2\",\"currentEnvironmentTemperature\":10}")));
    
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    
}
