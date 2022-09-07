package de.kosmos_lab.platform.web.servlets.scope;

import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.exceptions.GroupNotFoundException;
import de.kosmos_lab.platform.exceptions.NoAccessToScope;
import de.kosmos_lab.platform.exceptions.NotObjectSchemaException;
import de.kosmos_lab.platform.exceptions.SchemaNotFoundException;
import de.kosmos_lab.platform.exceptions.ScopeNotFoundException;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
		path = "/scope/addgroup",
		userLevel = 1
)
public class ScopeAddGroupServlet extends KosmoSAuthedServlet {

	private static final String FIELD_GROUP = "group";
	private static final String FIELD_SCOPE = "scope";
	public ScopeAddGroupServlet(KosmoSWebServer webServer, IController controller, int level) {
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
					@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE), description = "The group was added successfully"),
					//@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
					//@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),
					//@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
			})
	public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

			
			throws   NoAccessToScope, ScopeNotFoundException, ParameterNotFoundException, GroupNotFoundException, UnauthorizedException {
		String sname = request.getString(FIELD_SCOPE);
		String gname = request.getString(FIELD_GROUP);
		
		try {
			Scope scope = controller.getScope(sname, CacheMode.CACHE_AND_PERSISTENCE);
			
			if (scope.hasAdmin(request.getKosmoSUser()) || request.getKosmoSUser().isAdmin()) {
					Group u = controller.getGroup(gname, CacheMode.CACHE_AND_PERSISTENCE);
					if (u != null) {
						controller.addScopeGroup(scope, u);
						response.setStatus(de.kosmos_lab.web.server.WebServer.STATUS_NO_RESPONSE);
						return;
					}

			}
			throw new NoAccessToScope(scope);
			
		} catch (NotFoundInPersistenceException ex) {
			throw new ScopeNotFoundException(sname);
		}
		
	}
	
	
}

