package de.kosmos_lab.platform.web.servlets.group;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.exceptions.GroupAlreadyExistsException;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;

@ApiEndpoint(path = "/group/add", userLevel = 1)
public class GroupAddServlet extends KosmoSAuthedServlet {

    public final static String FIELD_NAME = "name";

    public GroupAddServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"group"},
            summary = "add",
            description = "Create a new group",
            requestBody = @RequestBody(

                    required = true,
                    content = {

                            @Content(
                                    examples = {
                                            @ExampleObject(
                                                    name = "add group with name 'testGroup'",
                                                    value = "{\"" + FIELD_NAME + "\":\"testGroup\"}"
                                            ),

                                    },
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = FIELD_NAME,
                                                    schema = @Schema(
                                                            description = "name of the new group.",
                                                            type = SchemaType.STRING,
                                                            minLength = 3,
                                                            required = true

                                                    )

                                            ),

                                    }

                            )
                    }),
            responses = {

                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), ref = "#/components/responses/scopeGet", description = "The group was added successfully"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a group with that name."),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),

            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, ParameterNotFoundException, GroupAlreadyExistsException, UnauthorizedException {
        String group_name = request.getString(FIELD_NAME);
        if (group_name.length() < 3) {
            response.sendError(de.kosmos_lab.web.server.WebServer.STATUS_UNPROCESSABLE, "group name is not long enough (minLength is 3)");
            return;
        }


        controller.addGroup(group_name, request.getKosmoSUser());
        response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);

    }

}

