package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/group/get"}, loadOnStartup = 1)
public class GroupGetServlet extends AuthedServlet {


    public GroupGetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }

    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ParameterNotFoundException, GroupNotFoundException {


        try {
            String sname = request.getString("name");
            if (sname != null) {
                Group group = controller.getGroup(sname, CacheMode.CACHE_AND_PERSISTENCE);
                sendJSON(request, response, group.toJSON());
                return;


            }


        } catch (ParameterNotFoundException ex) {

        }
        Group group = controller.getGroup(request.getInt("id"), CacheMode.CACHE_AND_PERSISTENCE);
        sendJSON(request, response, group.toJSON());
        return;


    }


}

