package CI.HTTP;

import common.CommonBase;
import de.kosmos_lab.utils.KosmosFileUtils;
import de.kosmos_lab.kosmos.data.KosmoSUser;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class TestKREE {
    
    
    
    @Test(dependsOnGroups = {"createUser"})
    public void testKreeMXML() {
        String uname1 = CommonBase.clientAdmin.getUserName();
        Assert.assertNotNull(uname1);
        KosmoSUser u1 = (KosmoSUser) CommonBase.controller.getUser(uname1);
        Assert.assertNotNull(u1);
        String uname2 = CommonBase.clientUser.getUserName();
        Assert.assertNotNull(uname2);
        KosmoSUser u2 = (KosmoSUser) CommonBase.controller.getUser(uname2);
        Assert.assertNotNull(u2);
        String uname3 = CommonBase.clientUser2.getUserName();
        Assert.assertNotNull(uname3);
        KosmoSUser u3 = (KosmoSUser) CommonBase.controller.getUser(uname3);
        Assert.assertNotNull(u3);


        File ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".xml");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".xml");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".xml");
        Assert.assertFalse(ruleFile.exists());
        
        String xml;
        ContentResponse response;
        
        
        response = CommonBase.clientUser.getResponse("/kree/loadXML", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "XML Loading did fail");
        xml = response.getContentAsString();
        Assert.assertEquals(xml,"<xml xmlns=\"https://developers.google.com/blockly/xml\">\n</xml>","empty XML was not correct");
        String postXML = "<xml xmlns=\"https://developers.google.com/blockly/xml\"><block type=\"text_print\" id=\"Wa+Xh46KKAx44(^88qwb\" x=\"388\" y=\"213\"><value name=\"TEXT\"><shadow type=\"text\" id=\"BR%,@t_#iTEW4Y70c)y~\"><field name=\"TEXT\">abc</field></shadow></value></block></xml>";
        response = CommonBase.clientUser.getResponse("/kree/saveXML", HttpMethod.POST,postXML);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "XML saving did fail");
        response = CommonBase.clientUser.getResponse("/kree/loadXML", HttpMethod.GET);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "XML Loading did fail");
        xml = response.getContentAsString().trim();
        Assert.assertEquals(xml,postXML,"posted XML was not correct");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".xml");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".xml");
        Assert.assertTrue(ruleFile.exists());
        xml = KosmosFileUtils.readFile(ruleFile).trim();
        Assert.assertEquals(xml,postXML,"XML in File was not correct");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".xml");
        Assert.assertFalse(ruleFile.exists());
        
    
    }
    private static String normalizeLineEnds(String s) {
        return s.replace("\r\n", "\n").replace('\r', '\n');
    }
    @Test(dependsOnGroups = {"createUser"})
    public void testKreePython() {
        String uname1 = CommonBase.clientAdmin.getUserName();
        Assert.assertNotNull(uname1);
        KosmoSUser u1 = (KosmoSUser) CommonBase.controller.getUser(uname1);
        Assert.assertNotNull(u1);
        String uname2 = CommonBase.clientUser.getUserName();
        Assert.assertNotNull(uname2);
        KosmoSUser u2 = (KosmoSUser) CommonBase.controller.getUser(uname2);
        Assert.assertNotNull(u2);
        String uname3 = CommonBase.clientUser2.getUserName();
        Assert.assertNotNull(uname3);
        KosmoSUser u3 = (KosmoSUser) CommonBase.controller.getUser(uname3);
        Assert.assertNotNull(u3);
        File ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        
        String python;
        ContentResponse response;
        String postPython = "test";
        response = CommonBase.clientUser.getResponse("/kree/savePython", HttpMethod.POST,postPython);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "XML saving did fail");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".py");
        Assert.assertTrue(ruleFile.exists());
        python = KosmosFileUtils.readFile(ruleFile).trim();
        Assert.assertEquals(python,postPython,"Python in File was not correct");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
    
    
        postPython = "import sys"+System.getProperty("line.separator")+"print('test')"+System.getProperty("line.separator");
        response = CommonBase.clientUser.getResponse("/kree/savePython", HttpMethod.POST,postPython);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200, "XML saving did fail");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".py");
        Assert.assertTrue(ruleFile.exists());
        python = KosmosFileUtils.readFile(ruleFile);
        Assert.assertEquals(normalizeLineEnds(python),normalizeLineEnds(postPython),"Python in File was not correct");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        String postPython2 = "import os"+System.getProperty("line.separator")+"print('test')"+System.getProperty("line.separator");
        response = CommonBase.clientUser.getResponse("/kree/savePython", HttpMethod.POST,postPython2);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 409, "XML saving did NOT fail");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u1.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u2.getID()+".py");
        Assert.assertTrue(ruleFile.exists());
        python = KosmosFileUtils.readFile(ruleFile);
        Assert.assertEquals(normalizeLineEnds(python),normalizeLineEnds(postPython),"Python in File was not correct");
        ruleFile = new File(CommonBase.controller.getRulesService().getRuleDir()+"/"+u3.getID()+".py");
        Assert.assertFalse(ruleFile.exists());
    }
}
