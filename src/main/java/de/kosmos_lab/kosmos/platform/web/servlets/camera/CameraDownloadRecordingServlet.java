package de.kosmos_lab.kosmos.platform.web.servlets.camera;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.NoAccessToRecording;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
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
            summary = "download recording",
            description = "Download a recording by filename",
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
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK), content = {@Content(mediaType = "video/*")}, description = "This is the binary content of actual file that was requested"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/CameraNotFoundError"),
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException, NoAccessToRecording {
        String filename = StringFunctions.filterFilename(request.getParameter(FIELD_FILENAME));
        byte[] content = controller.getRecording(request.getKosmoSUser(), filename);
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        try {

            response.getOutputStream().write(content);
            return;
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("exception while parsing camera {}", ex.getMessage(), ex);

        }

    }

}

