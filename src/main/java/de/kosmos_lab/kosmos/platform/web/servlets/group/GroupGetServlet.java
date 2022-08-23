package de.kosmos_lab.kosmos.platform.web.servlets.group;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.ArraySchema;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiResponse(
        componentName="groupGet",
        responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE),
        description = "Details about the group",

        content = @Content(

                mediaType = MediaType.APPLICATION_JSON,
                examples = {
                        @ExampleObject(
                                name="testGroup",
                                value="{\""+ Group.FIELD_NAME+"\":\"testGroup\",\""+Group.FIELD_ID+"\":18,\""+Group.FIELD_USERS+"\":[{\"name\":\"user7\",\"id\":7}],\""+Group.FIELD_ADMINS+"\":[{\"name\":\"admin\",\"id\":1}]}"
                        )
                },
                schemaProperties = {
                        @SchemaProperty(
                                name = Group.FIELD_NAME,
                                schema = @Schema(type = SchemaType.STRING, description = "The name of the group")
                        ),
                        @SchemaProperty(
                                name = Group.FIELD_ID,
                                schema = @Schema(type = SchemaType.INTEGER, description = "The ID of the group")
                        ),
                        @SchemaProperty(
                                name = Group.FIELD_ADMINS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of users with admin level access to the group",
                                                ref = "#/components/schemas/userNameID"
                                        )
                                )
                        ),
                        @SchemaProperty(
                                name = Group.FIELD_USERS,
                                array = @ArraySchema(
                                        uniqueItems = true,
                                        arraySchema = @Schema(
                                                description = "List of users with user level access to the group",
                                                ref = "#/components/schemas/userNameID"
                                        )
                                )
                        ),

                }))
@WebServlet(urlPatterns = {"/group/get"}, loadOnStartup = 1)
public class GroupGetServlet extends AuthedServlet {
    public final static String FIELD_NAME = "name";
    public final static String FIELD_ID = "id";

    public GroupGetServlet(WebServer webServer, IController controller) {
        super(webServer, controller);
    }
    @Operation(
            tags = {"group"},
            summary = "get",
            description = "get information about the group",
            parameters = {
                    @Parameter(
                            description = "The name of the group to get detail about. Use either 'name' or 'id'.",
                            in = ParameterIn.QUERY,
                            name = FIELD_NAME,
                            schema = @Schema(
                                    type = SchemaType.STRING
                            )
                    ),
                    @Parameter(
                            description = "The ID of the group to get detail about. Use either 'name' or 'id'.",
                            in = ParameterIn.QUERY,
                            name = FIELD_ID,
                            schema = @Schema(
                                    type = SchemaType.INTEGER
                            )
                    ),

            },

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK),ref = "#/components/responses/groupGet"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            }
    )
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ParameterNotFoundException, GroupNotFoundException {


        try {
            String sname = request.getString(FIELD_NAME);
            if (sname != null) {
                Group group = controller.getGroup(sname, CacheMode.CACHE_AND_PERSISTENCE);
                sendJSON(request, response, group.toJSON());
                return;


            }
        } catch (ParameterNotFoundException ex) {

        }
        Group group = controller.getGroup(request.getInt(FIELD_ID), CacheMode.CACHE_AND_PERSISTENCE);
        sendJSON(request, response, group.toJSON());
        return;


    }


}

