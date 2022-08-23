package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.headers.Header;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToRecording;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.utils.StringFunctions;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(
        path = "/camera/recording/download",
        userLevel = 1
)
public class CameraDownloadRecordingServlet extends AuthedServlet {
    
    public static final String FIELD_FILENAME = "filename";
    
    public CameraDownloadRecordingServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    
    @Operation(
            tags = {"camera"},
            summary = "Download camera recording.",
            description = "Download a recording by filename. You can only download recordings you started yourself.",
            parameters = {
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = FIELD_FILENAME,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            )
                    )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),
                            headers = {
                                    @Header(
                                            name = "Content-Disposition",
                                            description = "Content-Disposition is used here to mark the file as an attachment/download with the given filename. If you want to download it with wget you need to add the '--content-disposition' flag.",
                                            schema = @Schema(type = SchemaType.STRING,example = "attachment; filename=\"camera1_2022-03-22_17-51-32.mp4\"")
                                            
                                    )
                            },
                            content = {
                                    @Content(mediaType = "video/*")
                            },
                            description = "This is the binary content of actual file that was requested."),
                    /*@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/CameraNotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),*/
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, NoAccessToRecording {
        String filename = StringFunctions.filterFilename(request.getParameter(FIELD_FILENAME));
        byte[] content = controller.getRecording(request.getKosmoSUser(), filename);

        try {
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
            response.getOutputStream().write(content);
            return;
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("exception while parsing camera {}", ex.getMessage(), ex);
        }
        
    }
    
}

