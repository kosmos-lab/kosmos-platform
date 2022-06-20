package de.kosmos_lab.kosmos.data;

import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.utils.LocalSchemaClient;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;


/**
 * DataSchema is used to encapsulate the JSONSchema
 */
public class DataSchema {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("DataSchema");
    private final static Random random = new Random();
    private ObjectSchema schema;
    private JSONObject rawSchema;

    public DataSchema(@Nonnull File file) throws IOException, NotObjectSchemaException {
        FileReader f = new FileReader(file, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(new JSONTokener(f));
        load(json);
    }

    public JSONObject getRawSchema() {
        return this.rawSchema;
    }

    public ObjectSchema getSchema() {
        return this.schema;
    }

    public DataSchema(@Nonnull InputStream stream) throws NotObjectSchemaException {
        JSONObject json = new JSONObject(new JSONTokener(stream));
        load(json);
    }


    public void load(@Nonnull JSONObject json) throws NotObjectSchemaException {
        if (!json.has("$schema")) {
            json.put("$schema", "http://json-schema.org/draft-07/schema#");//force v7 if unknown
        }
        this.rawSchema = json;

        SchemaLoader loader = SchemaLoader.builder()
                .schemaClient(new LocalSchemaClient("https://kosmos-lab.de/"))
                .schemaJson(json)
                //.resolutionScope("classpath://schema/") // setting the default resolution scope
                .build();

        Schema s = loader.load().build();
        if (s instanceof ObjectSchema) {
            this.schema = (ObjectSchema) s;
        } else {
            throw new NotObjectSchemaException();
        }

    }

    public DataSchema(@Nonnull JSONObject json) throws NotObjectSchemaException {
        load(json);

    }

    public void validate(@Nonnull JSONObject data) throws ValidationException {

        schema.validate(data);

    }

    public boolean definesProperty(@Nonnull String key) {
        return this.schema.definesProperty(key);
    }

@CheckForNull public static Object getExample(@Nonnull Schema s) {

        logger.info("getting an example value for " + s.getId() + " type:" + s.getClass().toString());


        if (s instanceof NumberSchema) {
            NumberSchema ns = (NumberSchema) s;
            if (!ns.requiresInteger()) {
                double maximum = 1000000;
                Number max = ns.getMaximum();
                if (max != null) {
                    try {
                        maximum = max.doubleValue();
                    } catch (NumberFormatException e) {

                    }
                }
                double minimum = -1000000;
                Number min = ns.getMinimum();
                if (min != null) {
                    try {
                        minimum = min.doubleValue();
                    } catch (NumberFormatException e) {

                    }
                }
                double multipleOf = 1;
                Number multi = ns.getMultipleOf();
                if (multi != null) {
                    try {

                        multipleOf = multi.doubleValue();
                    } catch (NumberFormatException e) {

                    }
                }

                double v;
                if (multipleOf != 0.0d) {
                    int bound = (int) ((maximum - minimum) / multipleOf);
                    v = random.nextInt(bound);

                    if (multipleOf != 0.0d) {
                        v *= multipleOf;
                    }
                    v += minimum;
                } else {
                    double bound = (maximum - minimum);
                    v = random.nextDouble() * bound;
                    v += minimum;


                }
                logger.info("getting an example value for " + s.getId() + " from " + minimum + " to " + maximum + " in " + multipleOf + " steps : " + v);
                return v;
            } else {
                int maximum = Integer.MAX_VALUE;
                Number max = ns.getMaximum();
                if (max != null) {
                    try {

                        maximum = max.intValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int minimum = Integer.MIN_VALUE;
                Number min = ns.getMinimum();
                if (min != null) {
                    try {

                        minimum = min.intValue();
                    } catch (NumberFormatException e) {

                    }
                }
                int multipleOf = 1;
                Number multi = ns.getMultipleOf();
                if (multi != null) {
                    try {

                        multipleOf = multi.intValue();
                        if (multipleOf == 0) {
                            multipleOf = 1;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                logger.info("getting an example value for " + s.getId() + " from " + minimum + " to " + maximum + " in " + multipleOf + " steps: ");
                int bound;
                if (maximum == Integer.MAX_VALUE && minimum == Integer.MIN_VALUE) {
                    bound = Integer.MAX_VALUE / multipleOf;
                } else {
                    bound = (maximum - minimum) / multipleOf;
                }
                int v = random.nextInt(bound);
                v *= multipleOf;
                v += minimum;


                logger.info("got value: {}", v);
                return v;

            }
        } else if (s instanceof EnumSchema) {
            EnumSchema es = (EnumSchema) s;
            List<Object> lst = es.getPossibleValuesAsList();
            int idx = random.nextInt(lst.size());
            return lst.get(idx);
        } else if (s instanceof StringSchema) {

            int leftLimit = 48; // numeral '0'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;


            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            return generatedString;

        } else if (s instanceof BooleanSchema) {
            return random.nextBoolean();
        } else if (s instanceof CombinedSchema) {
            CombinedSchema cs = (CombinedSchema) s;
            for (Schema sub : cs.getSubschemas()) {
                if (sub instanceof EnumSchema) {
                    Object v = getExample(sub);
                    if (v != null) {
                        return v;
                    }
                }
            }
            for (Schema sub : cs.getSubschemas()) {
                Object v = getExample(sub);
                if (v != null) {
                    return v;
                }
            }
        }


        return null;

    }
}
