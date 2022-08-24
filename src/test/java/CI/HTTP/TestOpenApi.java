package CI.HTTP;

import de.kosmos_lab.web.server.OpenApiParser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestOpenApi {
    @Test(dependsOnGroups = {"login"})
    public void testExamples() {
        Assert.assertEquals(OpenApiParser.checkExamples().x, 0, "Did have broken tests for OpenApi examples");
    }

}
