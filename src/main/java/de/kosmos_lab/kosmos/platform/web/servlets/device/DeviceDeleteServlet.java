package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;


@ApiEndpoint(
        path = "/device/delete",
        userLevel = 1
)
public class DeviceDeleteServlet extends AuthedServlet {
    
    
    public DeviceDeleteServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"device"},
            summary = "delete",
            description = "delete a device",
            parameters = {@Parameter(name = "uuid",
                    in = ParameterIn.QUERY,
                    schema = @Schema(
                            description = "The uuid of the device to delete",
                            type = SchemaType.STRING,
                            minLength = 3,
                            required = true
                    )
            )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_RESPONSE), description = "The device was removed successfully"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response) throws ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope {
        
        
        String id = request.getUUID();
        
        
        Device d = controller.getDevice(id);
        if (d == null) {
            throw new DeviceNotFoundException(id);
        }
        IUser u = request.getKosmoSUser();
        if (u != null) {
            if (d.hasDelScope()) {
                if (d.canDel(u)) {
                    
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    
                    return;
                }
            } else {
                if (getSource(request).getSourceName().equalsIgnoreCase(d.getSource().getSourceName())) {
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    
                    return;
                } else if (u.isAdmin()) {
                    controller.deleteDevice(server, d);
                    response.setStatus(STATUS_NO_RESPONSE);
                    return;
                }
            }
            response.setStatus(STATUS_FORBIDDEN);
            return;
        }
        
        
    }
    
    
}

