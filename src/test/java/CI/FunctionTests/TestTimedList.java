package CI.FunctionTests;

import de.kosmos_lab.platform.data.TimedList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTimedList {

    @SuppressFBWarnings("GC_UNRELATED_TYPES")
    @Test
    public void TestTimedList() {
        TimedList list = new TimedList(500);
        list.addEntry("Test");
        list.addEntry("foo");
        list.addEntry("bar");
        Assert.assertTrue(list.getEntries().contains("Test"), "Test could not be found again");
        Assert.assertTrue(list.getEntries().contains("foo"), "Test could not be found again");
        Assert.assertTrue(list.getEntries().contains("bar"), "Test could not be found again");
        try {
            Thread.sleep(600);
            Assert.assertFalse(list.getEntries().contains("Test"), "Test could be found again after timout");
            Assert.assertFalse(list.getEntries().contains("foo"), "Test could be found again after timout");
            Assert.assertFalse(list.getEntries().contains("bar"), "Test could be found again after timout");
            list.addEntry("Test2");
            Thread.sleep(100);
            Assert.assertTrue(list.getEntries().contains("Test2"), "Test could not be found again");
            Assert.assertFalse(list.getEntries().contains("Test"), "Test could be found again after timout");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
