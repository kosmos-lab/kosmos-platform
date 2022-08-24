package de.kosmos_lab.platform.web.servlets.schreibtrainer;

import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.TimedList;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.DeviceAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;

@ApiEndpoint(
        path = "/schreibtrainer/word",
        userLevel = -1

)
public class SchreibtrainerWordServlet extends KosmoSServlet {

    public static final String FIELD_TEXT = "text";
    public SchreibtrainerWordServlet(KosmoSWebServer webServer, IController controller) {
        super(webServer, controller);

    }

    @Operation(
            tags = {"schreibtrainer"},
            summary = "word",
            description = "Add a word to a pen",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = SchreibtrainerConstants.FIELD_UUID,
                                                    schema = @Schema(
                                                            description = "The UUID of the pen to add the text to",
                                                            type = SchemaType.STRING,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = FIELD_TEXT,
                                                    schema = @Schema(
                                                            description = "The text you want to add",
                                                            type = SchemaType.STRING,
                                                            required = true
                                                    )
                                            ),


                                    }


                            )
                    }
            ),

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), description = "The word was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response) throws ParameterNotFoundException {
        
        String uuid = request.getParameter("uuid");
        //logger.info("length of content: {}",request.getContentLength());
        JSONObject o = request.getBodyAsJSONObject();
        if (o == null) {
            throw new ParameterNotFoundException("body");
        }
        if (uuid == null) {
            uuid = o.optString("uuid", null);
            
        }
        if (uuid == null) {
            throw new ParameterNotFoundException("uuid");
        }
        
        String newword = o.getString("text");
        logger.warn("got new word {} on {}: ", uuid, newword);
        try {
            Device device = SchreibtrainerConstants.getDevice(this.controller, server, uuid);
            TimedList wl = SchreibtrainerConstants.getWordList(this.controller, server, device);
            wl.addEntry(newword);
            //device.set("wordList", wl.toJSONArray(), false);
            
            device.updateFromJSON(this.server,new JSONObject().put("text", newword).put("wordList",wl.toJSONArray()),controller.getSource(SchreibtrainerConstants.SOURCENAME));
            sendJSON(request, response, o);
            return;
            
        } catch (SchemaNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE);
            return;
            
        }
        
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_ERROR);
        return;
        
    }
    
    
}

