package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiEndpoint(
        path = "/schema/delete",
        userLevel = 1

)
public class SchemaDeleteServlet extends AuthedServlet {
    
    
    public SchemaDeleteServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    @Operation(
            tags = {"schema"},
            summary = "delete",
            description = "Deletes the schema given by the id",
            parameters = {@Parameter(in= ParameterIn.QUERY,name="id",schema = @Schema(type = SchemaType.STRING),description = "the $id of the schema (most likely its an URI)")},

            responses = {

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND), description = "The schema was not found"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The schema was deleted"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_CONFLICT), description = "The schema cannot be deleted - it is still in use!"),

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, ParameterNotFoundException {
        String id = request.getString("id");
        DataSchema s = controller.getSchema(id);
        
        if (s != null) {
            if (this.controller.getPersistence().getNumberOfDevicesWithSchema(s) == 0) {
                
                this.controller.deleteSchema(s);
                response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_CONFLICT);
            return;
        }
        
        
        throw new SchemaNotFoundException(id);
        
        
    }
    
    
}

