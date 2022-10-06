package de.kosmos_lab.platform.web.servlets;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.exceptions.NoAccessException;
import de.kosmos_lab.platform.exceptions.NoAccessToGroup;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.NotFoundException;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.web.server.servlets.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.everit.json.schema.ValidationException;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class KosmoSServlet extends BaseServlet {
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmoSServlet");
    protected final IController controller;

    ;
    protected final KosmoSWebServer server;
    final ALLOW_AUTH allow_auth;

    public KosmoSServlet(KosmoSWebServer server, IController controller) {
        this(server, controller, ALLOW_AUTH.PARAMETER_AND_HEADER);
    }

    public KosmoSServlet(KosmoSWebServer server, IController controller, ALLOW_AUTH allow_auth) {
        super(server);
        this.server = server;
        this.controller = controller;
        this.allow_auth = allow_auth;
        logger.info("created servlet {}", this.getClass());
    }


    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws UnauthorizedException, ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessToScope, NoAccessToGroup, NoAccessException {

        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response)

            throws IOException {

        //super.doDelete(request, response);
        try {
            if (this.isAllowed(request, response)) {

                addCORSHeader(request, response);

                delete(new KosmoSHttpServletRequest(request), response);

            }
        } catch (Exception e) {
            handleException(request, response, e);

        }

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)

            throws IOException {
        try {
            if (this.isAllowed(request, response)) {

                addCORSHeader(request, response);
                get(new KosmoSHttpServletRequest(request), response);

            }
        } catch (Exception e) {
            handleException(request, response, e);

        }
    }

    public void doOptions(HttpServletRequest request, HttpServletResponse response)

            throws IOException {
        addCORSHeader(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)

            throws IOException {

        //logger.info("HITTING doPOST");
        try {
            if (this.isAllowed(request, response)) {

                addCORSHeader(request, response);

                post(new KosmoSHttpServletRequest(request), response);


            }
        } catch (Exception e) {
            handleException(request, response, e);

        }

    }

    public void doPut(HttpServletRequest request, HttpServletResponse response)

            throws IOException {
        super.doPut(request, response);
        try {
            if (this.isAllowed(request, response)) {

                addCORSHeader(request, response);

                put(new KosmoSHttpServletRequest(request), response);

            }
        } catch (Exception e) {
            handleException(request, response, e);

        }

    }

    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws UnauthorizedException, ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessException, NotFoundInPersistenceException {
        //logger.info("HITTING GET");

        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }


    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws UnauthorizedException, ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NotFoundInPersistenceException, AlreadyExistsException, NoAccessException {
        //logger.info("HITTING POST");
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }

    public void put(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws UnauthorizedException, ServletException, IOException, NotObjectSchemaException, ParameterNotFoundException, NotFoundException, ValidationException, NoAccessToScope, NoAccessToGroup {
        //logger.info("HITTING PUT");
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_METHOD_NOT_ALLOWED);
    }



    public enum ALLOW_AUTH {HEADER_ONLY, PARAMETER_AND_HEADER}


}
