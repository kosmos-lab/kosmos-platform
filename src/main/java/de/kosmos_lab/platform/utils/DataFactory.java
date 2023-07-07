package de.kosmos_lab.platform.utils;

import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DataFactory {

    private static boolean allRead = false;
    private static final HashMap<String, DataSchema> cache = new HashMap<>();

    public static DataSchema getSchema(File f) throws NotObjectSchemaException {

        if (f.exists()) {
            try {
                JSONObject json = new JSONObject(new JSONTokener(new FileReader(f, StandardCharsets.UTF_8)));

                DataSchema schema = new DataSchema(json);
                if (json.has("$id")) {
                    cache.put(json.getString("$id"), schema);
                }

                try {
                    cache.put(f.getCanonicalPath(), schema);
                } catch (IOException e) {

                }
                return schema;
            } catch (FileNotFoundException e) {
                //should never ever happen...
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;

    }

    public static DataSchema getSchema(String name) throws NotObjectSchemaException {
        DataSchema schema = cache.get(name);
        if (schema == null) {
            File f;
            if (!name.endsWith(".json")) {
                f = new File("schema/" + new File(name).getName() + ".json"); //very simple sanity check for the filename
            } else {
                f = new File("schema/" + new File(name).getName()); //very simple sanity check for the filename
            }

            schema = getSchema(f);
            if (schema == null) {
                if (!allRead) {
                    File dir = new File("schema/");
                    if (dir.exists()) {
                        File[] filelist = dir.listFiles();
                        if (filelist != null) {
                            for (File f2 : filelist) {
                                if (f2.getName().endsWith(".json")) {
                                    getSchema(f2);
                                }
                            }
                        }
                    }
                    allRead = true;
                    schema = cache.get(name); //try again
                }
            }
        }

        return schema;

    }
}
