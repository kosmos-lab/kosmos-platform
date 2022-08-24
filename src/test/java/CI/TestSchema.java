package CI;

import de.kosmos_lab.platform.data.DataEntry;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestSchema {
    @Test(priority = 100,groups = {"testSchema"})
    public void testSchema() throws NotObjectSchemaException {
        DataSchema schema = new DataSchema(new JSONObject("{\n" +
                "  \"$id\": \"https://example.com/person.schema.json\",\n" +
                "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"title\": \"Person\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"firstName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's first name.\"\n" +
                "    },\n" +
                "    \"lastName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's last name.\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}"));
        schema.validate(new JSONObject("{\n" +
                "  \"firstName\": \"John\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"age\": 21\n" +
                "}"));
        try {
            schema.validate(new JSONObject("{\n" +
                    "  \"firstName\": \"John\",\n" +
                    "  \"lastName\": \"Doe\",\n" +
                    "  \"age\": -1\n" +
                    "}"));
            Assert.fail("Should not have validated it...");
        } catch (ValidationException e) {
        
        }
        try {
            schema.validate(new JSONObject("{\n" +
                    "  \"firstName\": \"John\",\n" +
                    "  \"lastName\": \"Doe\",\n" +
                    "  \"age\": \"100\"\n" +
                    "}"));
            Assert.fail("Should not have validated it...");
        } catch (ValidationException e) {
        
        }
    }
    
    
    @Test(dependsOnGroups = {"testSchema"},priority = 100,groups = {"testAllSchemas"})
    public void testAllSchemas() throws FileNotFoundException {
        File dir = new File("schema");
        File[] files = dir.listFiles();
        Assert.assertNotNull(files);
        for (File f : files) {
            if (!f.getName().endsWith(".example.json") && f.getName().endsWith(".json")) {
                //System.out.println("testing " + f.getAbsolutePath().toString());
                try {
                    JSONObject json = new JSONObject(new JSONTokener(new FileReader(f, StandardCharsets.UTF_8)));

                    DataSchema schema = null;
                    try {
                        schema = new DataSchema(json);
                        //schema.describe();
                        if (json.has("examples")) {
                            JSONArray arr = json.getJSONArray("examples");
                            for (int i = 0; i < arr.length(); i++) {

                                JSONObject example = arr.getJSONObject(i);
                                try {
                                    new DataEntry(schema, example,false);
                                } catch (ValidationException e) {

                                    Assert.fail("could not validate:\n " + schema.getSchema().getId() + " - " + example.toString() + "\n" + e.getErrorMessage());
                                }

                            }
                        }
                        if (json.has("failures")) {
                            JSONArray arr = json.getJSONArray("failures");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject example = arr.getJSONObject(i);
                                try {
                                    new DataEntry(schema, example,false);
                                    Assert.fail("should not have validated this!\n" + example.toString());
                                } catch (ValidationException e) {

                                }
                            }
                        }
                    } catch (NotObjectSchemaException e) {
                        //e.printStackTrace();
                    }

                } catch (IOException ex) {
                    Assert.fail("run into Exception ", ex);
                }
            }
            
        }
        
    }
    
    
}
