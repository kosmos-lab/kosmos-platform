package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.web.server.WebServer;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class TestSchema {
    @Test(dependsOnGroups = {"createUser"}, groups = {"addSchema"})
    public void addSSchema() throws FileNotFoundException {
        ContentResponse response = CommonBase.httpClientAdmin.getResponse("/schema/get?id=https%3A%2F%2Fkosmos-lab.de%2Fschema%2FMultiSensor.json", HttpMethod.GET);
        Assert.assertNotNull(response);

        JSONObject schema = new JSONObject(KosmosFileUtils.readFile(new File("schema/MultiSensor.json")));

        if (response.getStatus() == WebServer.STATUS_NOT_FOUND) {
            //Assert.assertEquals(response.getStatus(), 404, "Could not get MultiSensor!");

            response = CommonBase.httpClientAdmin.getResponse("/schema/add", HttpMethod.POST, schema);
            Assert.assertNotNull(response);
        }


        String buffer = response.getContentAsString();
        //System.out.println("buffer" + buffer);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Schema add did fail!");
        response = CommonBase.httpClientAdmin.getResponse("/schema/get?id=https%3A%2F%2Fkosmos-lab.de%2Fschema%2FMultiSensor.json", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), WebServer.STATUS_OK, "Could not get MultiSensor!");

        buffer = response.getContentAsString();
        JSONObject object = new JSONObject(buffer);
        Assert.assertEquals(object.get("$id"), schema.get("$id"));
        Assert.assertEquals(object.get("$schema"), schema.get("$schema"));
        Assert.assertEquals(object.get("additionalProperties"), schema.get("additionalProperties"));
        Assert.assertEquals(object.get("title"), schema.get("title"));
        Assert.assertEquals(object.get("type"), schema.get("type"));

        Assert.assertTrue(schema.has("properties"), "original Schema has no properties?!");
        Assert.assertTrue(object.has("properties"), "returned Schema has no properties?!");
        /*JSONObject rProp = object.getJSONObject("properties");
        JSONObject oProp = schema.getJSONObject("properties");
        
        
        //Assert.assertTrue(CommonBase.compare(rProp,oProp),"could not verify that json objects are the same");
        */

    }
}
