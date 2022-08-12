package de.kosmos_lab.kosmos.platform.web.servlets.schema;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.DataSchema;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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

                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), description = "The schema was not found"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The schema was deleted"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "The schema cannot be deleted - it is still in use!"),

                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, ParameterNotFoundException {
        String id = request.getString("id");
        DataSchema s = controller.getSchema(id);
        
        if (s != null) {
            if (this.controller.getPersistence().getNumberOfDevicesWithSchema(s) == 0) {
                
                this.controller.deleteSchema(s);
                response.setStatus(STATUS_NO_RESPONSE);
                return;
            }
            response.setStatus(STATUS_CONFLICT);
            return;
        }
        
        
        throw new SchemaNotFoundException(id);
        
        
    }
    
    
}

