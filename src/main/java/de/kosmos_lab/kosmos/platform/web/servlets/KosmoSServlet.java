package de.kosmos_lab.kosmos.platform.web.servlets;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToGroup;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotFoundException;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;


public class KosmoSServlet extends HttpServlet {
    final ALLOW_AUTH allow_auth;

    public enum ALLOW_AUTH {HEADER_ONLY, PARAMETER_AND_HEADER};

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSServlet");

    protected final IController controller;
    protected final WebServer server;

    public KosmoSServlet(WebServer server, IController controller) {
        this(server, controller, ALLOW_AUTH.PARAMETER_AND_HEADER);
    }

    public KosmoSServlet(WebServer server, IController controller, ALLOW_AUTH allow_auth) {
        this.server = server;
        this.controller = controller;
        this.allow_auth = allow_auth;
        logger.info("created servlet {}", this.getClass());
    }


    protected void addCORSHeader(HttpServletRequest req, HttpServletResponse response) {
        String origin = req.getHeader("Origin");
        if (origin == null || origin.length() == 0) {
            origin = "*";
        } else {
            origin = URLEncoder.encode(origin, StandardCharsets.UTF_8);
        }

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Cache-Control");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    protected boolean checkParameter(HttpServletRequest req, HttpServletResponse response, String[] keys) throws IOException {
        Enumeration<String> it = req.getParameterNames();
        HashMap<String, Boolean> missing = new HashMap<>();
        for (String k : keys) {
            missing.put(k, true);
        }


        while (it.hasMoreElements()) {
            String e = it.nextElement();

            missing.remove(e);
        }
        if (!missing.isEmpty()) {
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
            PrintWriter w = response.getWriter();
            for (String k : missing.keySet()) {
                w.println("missing parameter '" + k + "'");
            }
            return false;
        }
        return true;


    }

    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessToScope, NoAccessToGroup, NoAccessException {

        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        //super.doDelete(request, response);
        if (this.isAllowed(request, response)) {
            try {
                addCORSHeader(request, response);

                delete(new KosmoSHttpServletRequest(request), response);
            } catch (NotObjectSchemaException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
                response.getWriter().print("not object schema");
            } catch (ParameterNotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (NotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND);
                response.getWriter().print(e.getMessage());
            } catch (NoAccessException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                response.getWriter().print(e.getMessage());
            } catch (ValidationException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_VALIDATION_FAILED);
                response.getWriter().print(e.getMessage());
            } catch (JSONException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE);
                response.getWriter().print(e.getMessage());
            } catch (Exception e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
                e.printStackTrace();

            }
        }

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        if (this.isAllowed(request, response)) {
            try {
                addCORSHeader(request, response);
                get(new KosmoSHttpServletRequest(request), response);
            } catch (NotObjectSchemaException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
                response.getWriter().print("not object schema");
            } catch (ParameterNotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (NotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND);
                response.getWriter().print(e.getMessage());
            } catch (ValidationException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_VALIDATION_FAILED);
                response.getWriter().print(e.getMessage());
            } catch (NoAccessException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                response.getWriter().print(e.getMessage());
            } catch (JSONException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (Exception e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
                e.printStackTrace();

            }
        }

    }

    public void doOptions(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {
        addCORSHeader(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        //logger.info("HITTING doPOST");

        if (this.isAllowed(request, response)) {
            try {
                addCORSHeader(request, response);

                post(new KosmoSHttpServletRequest(request), response);
            } catch (NotObjectSchemaException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
                response.getWriter().print("not object schema");
            } catch (JSONException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (ParameterNotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (NotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND);
                response.getWriter().print(e.getMessage());
            } catch (NoAccessException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                response.getWriter().print(e.getMessage());
            } catch (ValidationException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_VALIDATION_FAILED);
                response.getWriter().print(e.getMessage());
            } catch (AlreadyExistsException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_CONFLICT);
                response.getWriter().print(e.getMessage());

            } catch (Exception e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
                e.printStackTrace();


            }

        }

    }

    public void doPut(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {
        super.doPut(request, response);
        if (this.isAllowed(request, response)) {
            try {
                addCORSHeader(request, response);

                put(new KosmoSHttpServletRequest(request), response);
            } catch (NotObjectSchemaException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FAILED);
                response.getWriter().print("not object schema");
            } catch (ParameterNotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_MISSING_VALUE);
                response.getWriter().print(e.getMessage());
            } catch (NotFoundException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND);
                response.getWriter().print(e.getMessage());
            } catch (ValidationException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_VALIDATION_FAILED);
                response.getWriter().print(e.getMessage());
            } catch (NoAccessException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN);
                response.getWriter().print(e.getMessage());
            } catch (JSONException e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_UNPROCESSABLE);
                response.getWriter().print(e.getMessage());
            } catch (Exception e) {
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_ERROR);
                e.printStackTrace();

            }
        }

    }

    public void get(KosmoSHttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessException, NotFoundInPersistenceException {
        //logger.info("HITTING GET");

        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }


    protected boolean isAllowed(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    public void options(KosmoSHttpServletRequest request, HttpServletResponse response) {

        response.setStatus(200);
        addCORSHeader(request.getRequest(), response);

    }

    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessToScope, NoAccessToGroup, NotFoundInPersistenceException, AlreadyExistsException, NoAccessException {
        //logger.info("HITTING POST");
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }

    public void put(KosmoSHttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessToScope, NoAccessToGroup {
        //logger.info("HITTING PUT");
        response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }

    public static void sendJSON(KosmoSHttpServletRequest req, HttpServletResponse response, JSONObject obj) throws IOException {
        response.setHeader("Content-Type", "application/json");
        try {
            String p = req.getParameter("pretty");
            if ("1".equals(p)) {
                response.getWriter().print(obj.toString(4));
            } else {
                response.getWriter().print(obj.toString());
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            response.getWriter().print(obj.toString());
        }

    }

    public static void sendJSON(KosmoSHttpServletRequest req, HttpServletResponse response, JSONArray obj) throws IOException {
        response.setHeader("Content-Type", "application/json");
        try {
            String p = req.getParameter("pretty");
            if ("1".equals(p)) {
                response.getWriter().print(obj.toString(4));
            } else {
                response.getWriter().print(obj.toString());
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            response.getWriter().print(obj.toString());
        }


    }

    public static void sendJWT(KosmoSHttpServletRequest req, HttpServletResponse response, String text) throws IOException {
        response.setHeader("Content-Type", "application/jwt");
        response.getWriter().print(text);


    }

    public static void sendText(KosmoSHttpServletRequest req, HttpServletResponse response, String text) throws IOException {
        response.setHeader("Content-Type", "text/plain");
        response.getWriter().print(text);
    }
    public static void sendHTML(KosmoSHttpServletRequest req, HttpServletResponse response, String text) throws IOException {
        response.setHeader("Content-Type", "text/html");
        response.getWriter().print(text);
    }
    public static void sendXML(KosmoSHttpServletRequest req, HttpServletResponse response, String text) throws IOException {
        response.setHeader("Content-Type", "application/xml");
        response.getWriter().print(text);
    }
}
