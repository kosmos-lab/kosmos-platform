package de.kosmos_lab.platform.web.servlets.device;

import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;

import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Device;

import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.http.HttpServletResponse;


@ApiEndpoint(
        path = "/device/delete",
        userLevel = 1
)
public class DeviceDeleteServlet extends KosmoSAuthedServlet {
    
    
    public DeviceDeleteServlet(KosmoSWebServer webServer, IController controller, int level) {
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
                   @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The device was removed successfully"),            })
    
    public void delete(KosmoSHttpServletRequest request, HttpServletResponse response)
 throws ParameterNotFoundException, DeviceNotFoundException, NoAccessToScope, NoAccessException {
        
        
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
                    
                    response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                    
                    return;
                }
            } else {
                if (getSource(request).getSourceName().equalsIgnoreCase(d.getSource().getSourceName())) {
                    controller.deleteDevice(server, d);
                    
                    response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                    
                    return;
                } else if (u.isAdmin()) {
                    controller.deleteDevice(server, d);
                    
                    response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
                    return;
                }
            }
            
            
            throw new NoAccessException("You are not allowed to delete this device.");
            
        }
        
        
    }
    
    
}

