package de.kosmos_lab.kosmos.platform.web.servlets.device;

import de.dfki.baall.helper.webserver.data.IUser;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Device;

import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.DeviceNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.http.HttpServletResponse;


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
            summary = "Delete a device",
            description = "Delete a device",
            parameters = {@Parameter(name = "uuid",
                    in = ParameterIn.QUERY,
                    schema = @Schema(
                            
                            description = "The uuid of the device to delete.",
                            type = SchemaType.STRING,
                            minLength = 3,
                            required = true
                    )
            )
            },
            responses = {
                   @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The device was removed successfully"),            })
    
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response) throws ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope, NoAccessException {
        
        
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
                    
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                    
                    return;
                }
            } else {
                if (getSource(request).getSourceName().equalsIgnoreCase(d.getSource().getSourceName())) {
                    controller.deleteDevice(server, d);
                    
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                    
                    return;
                } else if (u.isAdmin()) {
                    controller.deleteDevice(server, d);
                    
                    response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
            }
            
            
            throw new NoAccessException("You are not allowed to delete this device.");
            
        }
        
        
    }
    
    
}

