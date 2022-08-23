package de.kosmos_lab.kosmos.platform.web.servlets.openapi;

import de.dfki.baall.helper.webserver.exceptions.ParameterNotFoundException;
import de.dfki.baall.helper.webserver.annotations.Operation;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.doc.openapi.ApiEndpoint;
import de.dfki.baall.helper.webserver.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.OpenApiParser;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@ApiEndpoint(path ="/doc/openapi.yaml", userLevel = -1)
public class OpenApiServlet extends KosmoSServlet {
    public String cached = null;

    public OpenApiServlet(WebServer webServer, IController controller) {
        super(webServer, controller);

    }
    @Operation(
            tags = {"OpenApi"},
            summary = "openapi.yaml",
            description = "The generated openApi specification for this service in YAML format",
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.dfki.baall.helper.webserver.WebServer.STATUS_OK), description = "The generated openApi specification for this service"),
            }
    )
    @Override
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParameterNotFoundException {
        if (cached == null) {

            /*StringBuilder sb = new StringBuilder();



            Reflections r = new Reflections("");

            //r.getTypesAnnotatedWith()
            OpenApiToYaml.append("openapi","3.0.0",0,sb);
            OpenApiToYaml.appendItem("info",0,sb);
            OpenApiToYaml.append("description","# Kosmos Platform Synchron HTTP API \n" +
                            "### [Asyncron WS/MQTT Documentation](async.html) \n" +
                            "This is the OpenAPI 3.0 specifaction for KosmoS, it can be found on https://kosmos-lab.de/doc/openapi.yaml \n" +
                            "Please make sure you are logged in if you want to try to execute any request to the server.   \n" +
                            "You can simply login with the form injected to the top of the page. ",2,sb);
            OpenApiToYaml.append("title","KosmoS OpenAPI",2,sb);
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader("pom.xml"));
                OpenApiToYaml.append("version", model.getVersion(), 2, sb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            OpenApiToYaml.appendItem("components",0,sb);
            OpenApiToYaml.appendItem("securitySchemes",2,sb);
            OpenApiToYaml.appendItem("bearerAuth",4,sb);
            OpenApiToYaml.append("description","contains a JWT token obtainable from #post-/user/login",6,sb);
            OpenApiToYaml.append("type","http",6,sb);
            OpenApiToYaml.append("scheme","bearer",6,sb);
            OpenApiToYaml.append("bearerFormat","JWT",6,sb);
            OpenApiToYaml.appendItem("secret",4,sb);
            OpenApiToYaml.append("description","contains a secret known to both parties",6,sb);
            OpenApiToYaml.append("type","apiKey",6,sb);
            OpenApiToYaml.append("in","query",6,sb);
            OpenApiToYaml.append("name","token",6,sb);
            //sb.append("paths:\n");
            OpenApiToYaml.appendItem("paths",0,sb);
            for (Class<? extends KosmoSServlet> c : r.getSubTypesOf(KosmoSServlet.class)) {
                ApiEndpoint a = c.getAnnotation(ApiEndpoint.class);
                if (a != null) {
                    logger.info("found {}", a);
                    for (String method : new String[]{"get", "post", "delete"})
                        try {

                            Method m = c.getMethod(method, KosmoSHttpServletRequest.class, HttpServletResponse.class);
                            ApiEndpointMethod am = m.getAnnotation(ApiEndpointMethod.class);
                            if (am != null) {
                                OpenApiToYaml.append(a, am, 2, method, sb);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                }

            }
*/
            cached = OpenApiParser.getYAML();

        }
        sendText(request, response, cached);


    }

}

