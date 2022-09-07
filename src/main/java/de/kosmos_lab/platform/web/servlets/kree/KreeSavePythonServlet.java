package de.kosmos_lab.platform.web.servlets.kree;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/kree/savePython",
        userLevel = 1

)
public class KreeSavePythonServlet extends KosmoSAuthedServlet {
    
    
    //bad everywhere in the string
    private final static String[] bad = new String[]{"open(", "load(", "loads("};
    //bad only at the end of the string
    private final static String[] badEnds = new String[]{};
    //bad only at the start
    private final static String[] badStarts = new String[]{};
    private final static String[] allowedImports = new String[]{"import math","import random","from numbers import Number","import os.path", "import sys", "from kosmos import *", "import threading", "import time"};
    
    public KreeSavePythonServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);

    }
    @Operation(
            tags = {"kree"},
            summary = "save python",
            description = "Saves the python to persistence and executes it, will also be automatically restarted if it crashed an on start",
            requestBody = @RequestBody(
                    description = "the python of the blocks",
                    content = {
                            @Content(
                                    mediaType = "application/python")
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE),description = "Python saved successfully" ),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_CONFLICT), description = "The saving of this code was blocked - you are using unsafe code"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE),description = "Could not save python, the request body was empty." ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError")
            }
    )
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            
            throws IOException, UnauthorizedException {
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
                        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : bad) {
                    if (l.contains(b)) {
                        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : badEnds) {
                    if (l.endsWith(b)) {
                        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                for (String b : badStarts) {
                    if (l.startsWith(b)) {
                        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_VALIDATION_FAILED);
                        return;
                    }
                }
                
            }
            server.getRulesService().savePython(request.getKosmoSUser(), python);
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
            
            return;
        }
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE);
        return;
    }
    
    
}

