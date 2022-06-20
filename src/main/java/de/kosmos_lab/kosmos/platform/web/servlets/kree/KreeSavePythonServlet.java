package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/kree/savePython"}, loadOnStartup = 1)
public class KreeSavePythonServlet extends AuthedServlet {
    
    
    //bad everywhere in the string
    private final static String[] bad = new String[]{"open(", "load(", "loads("};
    //bad only at the end of the string
    private final static String[] badEnds = new String[]{};
    //bad only at the start
    private final static String[] badStarts = new String[]{};
    private final static String[] allowedImports = new String[]{"import math","import random","from numbers import Number","import os.path", "import sys", "from kosmos import *", "import threading", "import time"};
    
    public KreeSavePythonServlet(WebServer webServer, IController controller) {
        super(webServer, controller, 1);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException {
        String python = request.getBody();
        if (python.length()>0) {
            for (String l : python.split("\n")) {
                l = l.trim();
                if (l.contains("import ")) {
                    boolean isOk = false;
                    for (String i : allowedImports) {
                        if (l.equals(i)) {
                            isOk = true;
                            break;
                        }
                    }
                    if (!isOk) {
                        response.setStatus(STATUS_CONFLICT);
                        return;
                    }
                }
                for (String b : bad) {
                    if (l.contains(b)) {
                        response.setStatus(STATUS_CONFLICT);
                        return;
                    }
                }
                for (String b : badEnds) {
                    if (l.endsWith(b)) {
                        response.setStatus(STATUS_CONFLICT);
                        return;
                    }
                }
                for (String b : badStarts) {
                    if (l.startsWith(b)) {
                        response.setStatus(STATUS_CONFLICT);
                        return;
                    }
                }
                
            }
            server.getRulesService().savePython(request.getKosmoSUser(), python);
            response.setStatus(STATUS_OK);
            
            return;
        }
        response.setStatus(STATUS_FAILED);
        return;
    }
    
    
}

