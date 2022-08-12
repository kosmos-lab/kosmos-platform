package de.kosmos_lab.kosmos.platform.web.servlets.kree;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/kree/savePython",
        userLevel = 1

)
public class KreeSavePythonServlet extends AuthedServlet {
    
    
    //bad everywhere in the string
    private final static String[] bad = new String[]{"open(", "load(", "loads("};
    //bad only at the end of the string
    private final static String[] badEnds = new String[]{};
    //bad only at the start
    private final static String[] badStarts = new String[]{};
    private final static String[] allowedImports = new String[]{"import math","import random","from numbers import Number","import os.path", "import sys", "from kosmos import *", "import threading", "import time"};
    
    public KreeSavePythonServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }
    @Operation(
            tags = {"kree"},
            summary = "save python",
            description = "saves the block python to persistence and executes it",
            requestBody = @RequestBody(
                    description = "the python of the blocks",
                    content = {
                            @Content(

                                    mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_XML)

                    }

            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE),description = "Python saved successfully" ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), ref = "The saving of this code was blocked - you are using unsafe code"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
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
                        response.setStatus(STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : bad) {
                    if (l.contains(b)) {
                        response.setStatus(STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : badEnds) {
                    if (l.endsWith(b)) {
                        response.setStatus(STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : badStarts) {
                    if (l.startsWith(b)) {
                        response.setStatus(STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                
            }
            server.getRulesService().savePython(request.getKosmoSUser(), python);
            response.setStatus(STATUS_NO_RESPONSE);
            
            return;
        }
        response.setStatus(STATUS_FAILED);
        return;
    }
    
    
}

