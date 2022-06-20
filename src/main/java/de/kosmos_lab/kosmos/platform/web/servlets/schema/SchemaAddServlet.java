package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.dfki.baall.helper.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaAlreadyExistsException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(urlPatterns = {"/schema/add"}, loadOnStartup = 1)
public class SchemaAddServlet extends AuthedServlet {
    
    
    public SchemaAddServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
        
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, AlreadyExistsException {
        JSONObject o = request.getBodyAsJSONObject();
        if (o != null) {
            logger.info("schema add {}", o);
            if (o.has("$id")) {
                String id = o.getString("$id");
                if (id != null) {
                    try {

                        DataSchema s = controller.getSchema(id);
                        if (s != null) {
                            throw new SchemaAlreadyExistsException(id);
                        }
                    } catch (SchemaNotFoundException ex) {
                        //throw new SchemaNotFoundException(id);
                    }
                    DataSchema ds = new DataSchema(o);
                    controller.addSchema(ds);
                    response.getWriter().print(o);
                    response.setStatus(STATUS_OK);
                    return;
                }
            }
        }
        response.setStatus(STATUS_FAILED);
    }
    
    
}

