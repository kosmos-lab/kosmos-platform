package de.kosmos_lab.kosmos.platform.web.servlets.scope;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.enums.SchemaType;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;
import de.dfki.baall.helper.webserver.annotations.media.SchemaProperty;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.data.Group;
import de.kosmos_lab.kosmos.data.Scope;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GroupNotFoundException;
import de.kosmos_lab.kosmos.exceptions.NoAccessToScope;
import de.kosmos_lab.kosmos.exceptions.NotObjectSchemaException;
import de.kosmos_lab.kosmos.exceptions.SchemaNotFoundException;
import de.kosmos_lab.kosmos.exceptions.ScopeNotFoundException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
		path = "/scope/addgroup",
		userLevel = 1
)
public class ScopeAddGroupServlet extends AuthedServlet {

	private static final String FIELD_GROUP = "group";
	private static final String FIELD_SCOPE = "scope";
	public ScopeAddGroupServlet(WebServer webServer, IController controller,int level) {
		super(webServer, controller,level);
	}
	@Operation(
			tags = {"scope"},
			summary = "add group",
			description = "add a new group to a scope",
			requestBody = @RequestBody(
					required = true,
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schemaProperties = {
											@SchemaProperty(
													name = FIELD_SCOPE,
													schema = @Schema(
															description = "The scope to which to add the group",
															type = SchemaType.STRING,
															required = true
													)
											),
											@SchemaProperty(
													name = FIELD_GROUP,
													schema = @Schema(
															description = "The name of the group to add to the scope",
															type = SchemaType.STRING,
															required = true
													)
											),

									},
									examples = {
											@ExampleObject(
													name = "add group 'testGroup' to scope 'testScope'",
													value = "{\"" + FIELD_SCOPE + "\":\"testScope\",\"" + FIELD_GROUP + "\":\"testGroup\"}"
											),
									}
							)
					}
			),
			responses = {
					@ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE), description = "The group was added successfully"),
					@ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
					@ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
					@ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
			})
	public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
			
			throws ServletException, IOException, NotObjectSchemaException, SchemaNotFoundException, NoAccessToScope, NotFoundInPersistenceException, ScopeNotFoundException, ParameterNotFoundException, GroupNotFoundException {
		String sname = request.getString(FIELD_SCOPE);
		String gname = request.getString(FIELD_GROUP);
		
		try {
			Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
			
			if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
					Group u = controller.getGroup(gname, CacheMode.CACHE_AND_PERSISTENCE);
					if (u != null) {
						controller.addScopeGroup(scope, u);
						response.setStatus(de.dfki.baall.helper.webserver.WebServer.STATUS_NO_RESPONSE);
						return;
					}

			}
			throw new NoAccessToScope(scope);
			
		} catch (NotFoundInPersistenceException ex) {
			throw new ScopeNotFoundException(sname);
		}
		
	}
	
	
}

