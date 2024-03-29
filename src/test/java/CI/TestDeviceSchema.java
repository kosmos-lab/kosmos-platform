package CI;

import de.kosmos_lab.platform.data.DataEntry;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.utils.DataFactory;
import org.everit.json.schema.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDeviceSchema {


    @Test(groups = {"testDevice"}, priority = 100)
    public void testDevice() throws NotObjectSchemaException {
        DataSchema schema = DataFactory.getSchema("Device");
        Assert.assertNotNull(schema);

    }

    @Test(groups = {"testDeviceFullUri"}, priority = 100)
    public void testDeviceFullUri() throws NotObjectSchemaException {
        DataSchema schema = DataFactory.getSchema("https://kosmos-lab.de/schema/Device.json");
        Assert.assertNotNull(schema);
        new DataEntry(schema, "{\"apiUrl\":\"http://test.de\"}");
        try {
            new DataEntry(schema, "{\"apiUrl\":false}");
            Assert.fail("should not have parsed");
        } catch (ValidationException e) {

        }


    }

}
