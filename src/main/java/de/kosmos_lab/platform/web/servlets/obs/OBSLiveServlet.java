package de.kosmos_lab.platform.web.servlets.obs;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.StateUpdates;
import de.kosmos_lab.platform.exceptions.DeviceNotFoundException;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.Explode;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


@ApiEndpoint(
        path = "/obs/live",
        userLevel = 1
)
public class OBSLiveServlet extends KosmoSAuthedServlet {


    public static final Long DEFAULT_MAX_AGE = 120000L;

    public OBSLiveServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"obs"},
            summary = "live",
            description = "Get the information to show in OBS",
            parameters = {
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = "uuid",
                            required = true,
                            description = "A comma seperated list of all uuids you want to get the status of, does support wildcards",
                            array = @ArraySchema(
                                    minItems = 1,
                                    uniqueItems = true,
                                    arraySchema = @Schema(type = SchemaType.STRING)
                            ),
                            explode = Explode.TRUE,
                            examples = {
                                    @ExampleObject(name = "virt_sensor_temp_0,virt_sensor_temp_1", value = "virt_sensor_temp_0,virt_sensor_temp_1"),
                                    @ExampleObject(name = "virt_sensor_temp_0", value = "virt_sensor_temp_0"),
                                    @ExampleObject(name = "virt_sensor_temp_*", value = "virt_sensor_temp_*"),
                                    @ExampleObject(name = "*", value = "*")
                            }
                    ),
                    @Parameter(name = "type",
                            in = ParameterIn.QUERY,
                            schema = @Schema(
                                    description = "The type to return back",
                                    type = SchemaType.STRING,
                                    allowableValues = {"html", "json", "text"},
                                    required = false,
                                    defaultValue = "text"
                            )
                    ),
                    @Parameter(name = "maxAge",
                            in = ParameterIn.QUERY,
                            schema = @Schema(
                                    description = "The maximum age of a change to show it",
                                    type = SchemaType.NUMBER,
                                    required = false,
                                    defaultValue = "120000"
                            )
                    )
            },
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), description = "the wanted data", content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(
                                            type = SchemaType.OBJECT,
                                            description = "a name value pair with the changes that were seen in the timeframe, returned if type == 'json'"
                                    ),
                                    examples = {@ExampleObject(
                                            name = "JSON Example",
                                            value = "{\"virt_sensor_temp_0\":{\"currentEnvironmentTemperature\":{\"value\":17,\"age\":30445}},\"virt_sensor_temp_1\":{\"currentEnvironmentTemperature\":{\"value\":17,\"age\":19460}}}")}
                            ),
                            @Content(mediaType = MediaType.TEXT_HTML,
                                    schema = @Schema(
                                            type = SchemaType.STRING,
                                            description = "A HTML page showing the changes, returned if type == 'html'"
                                    ),
                                    examples = {@ExampleObject(
                                            name = "JSON Example",
                                            value = "<html>virt_sensor_temp_0:{\"currentEnvironmentTemperature\":17}<br>virt_sensor_temp_1:{\"currentEnvironmentTemperature\":17}<br></html>")}
                            ), @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "A string showing the changes, returned if type == 'text'"
                            ),
                            examples = {@ExampleObject(
                                    name = "JSON Example",
                                    value = "virt_sensor_temp_0:{\"currentEnvironmentTemperature\":17}\nvirt_sensor_temp_1:{\"currentEnvironmentTemperature\":17}")}
                    ),
                    }),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NOT_FOUND), ref = "#/components/responses/NotFoundError"),

                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_FORBIDDEN), ref = "#/components/responses/NoAccessError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)
            throws IOException, DeviceNotFoundException, ParameterNotFoundException, UnauthorizedException {

        String id = request.getUUID();
        String html = request.getString("html", null);

        boolean doHTML = html != null;
        String json = request.getString("json", null);
        boolean doJSON = json != null;

        String type = request.getString("type", null);
        if (type != null) {
            if (type.equals("json")) {
                doJSON = true;
            } else if (type.equals("html")) {
                doHTML = true;
            }
        }
        Long maxage = DEFAULT_MAX_AGE;
        try {
            String t = request.getString("maxAge", null);
            if (t != null) {
                maxage = Long.parseLong(t) * 1000;
                if (maxage > DEFAULT_MAX_AGE) {
                    maxage = DEFAULT_MAX_AGE;
                }
            }


        } catch (NumberFormatException ex) {


        }


        HashSet<String> uuids = new HashSet<String>();
        for (String u : id.split(",")) {
            if (u.contains("*")) {
                uuids.addAll(controller.getMatchingUUID(u));
            } else {
                uuids.add(u);
            }

        }
        HashMap<Device, StateUpdates> list = controller.getUpdates(uuids, maxage);
        if (doHTML) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {

                //sb.append(upd.uuid).append(':').append(upd.changes.toString()).append("\n");
                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    sb.append(entry.getKey().getName()).append(':');
                    JSONObject o2 = new JSONObject();
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), entry.getKey().get(change.getKey()));
                    }
                    sb.append(o2).append("<br>");
                }
            }
            sb.append("</html>");

            sendHTML(request, response, sb.toString().trim());
        } else if (doJSON) {
            JSONObject j = new JSONObject();
            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {
                JSONObject o2 = new JSONObject();

                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    //sb.append(entry.getKey()).append(':');
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), new JSONObject().put("value", entry.getKey().get(change.getKey())).put("age", change.getValue()));
                    }
                    j.put(entry.getKey().getUniqueID(), o2);
                }


            }
            sendJSON(request, response, j);
        } else {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<Device, StateUpdates> entry : list.entrySet()) {

                //sb.append(upd.uuid).append(':').append(upd.changes.toString()).append("\n");
                HashMap<String, Long> map = entry.getValue().map;
                if (!map.isEmpty()) {
                    sb.append(entry.getKey().getName()).append(':');
                    JSONObject o2 = new JSONObject();
                    for (Map.Entry<String, Long> change : map.entrySet()) {
                        o2.put(change.getKey(), entry.getKey().get(change.getKey()));
                    }
                    sb.append(o2).append('\n');
                }
            }
            sendText(request, response, sb.toString().trim());
        }


    }


}

