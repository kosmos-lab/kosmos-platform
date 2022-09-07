package de.kosmos_lab.platform.web.servlets.gesture;

import de.kosmos_lab.platform.gesture.data.Gesture;
import de.kosmos_lab.platform.gesture.data.Point;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.ObjectSchema;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;

import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import de.kosmos_lab.web.exceptions.UnauthorizedException;
import de.kosmos_lab.web.server.WebServer;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

@ArraySchema(
        minItems = 1,
        name = "pointList",
        arraySchema = @Schema(
                ref = "#/components/schemas/point"
        )
)
@ArraySchema(
        minItems = 2,
        maxItems = 2,
        name = "point",
        arraySchema = @Schema(
                description = "Array of x,y in this point [0] = x [1] = y",
                type = SchemaType.INTEGER
        )
)
@ArraySchema(
        minItems = 3,
        maxItems = 3,
        name = "pointStroke",
        arraySchema = @Schema(
                description = "Array of x,y,strokeId in this point [0] = x [1] = y [2] = strokeId ",

                type = SchemaType.INTEGER
        )
)
@ObjectSchema(
        componentName = "gestureInfo",
        properties = {
                @SchemaProperty(
                        name = "id",
                        schema = @Schema(
                                description = "The unique id of the gesture",
                                type = SchemaType.STRING,
                                required = true
                        )

                ),
                @SchemaProperty(
                        name = "name",
                        schema = @Schema(
                                description = "The name of the gesture",
                                type = SchemaType.STRING,
                                required = true
                        )

                ),

                @SchemaProperty(
                        name = "points",
                        array = @ArraySchema(

                                arraySchema = @Schema(
                                        ref = "#/components/schemas/pointStroke"
                                )
                        )
                )

        }
)
@ApiResponse(
        componentName = "gestureList",
        responseCode = @ResponseCode(statusCode = WebServer.STATUS_OK),
        description = "The current list of gestures",
        content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = {
                        @ExampleObject(
                                name = "T",
                                value = "[{\"name\":\"T\",\"id\":\"ni8m5p864nuu90uvto4jqmgd40\",\"points\":[[477,41,0],[478,42,0],[478,43,0],[478,45,0],[478,46,0],[478,50,0],[479,54,0],[479,60,0],[479,67,0],[479,76,0],[479,85,0],[479,96,0],[479,106,0],[479,117,0],[478,127,0],[477,140,0],[477,151,0],[476,161,0],[475,170,0],[474,179,0],[473,186,0],[473,193,0],[472,199,0],[471,205,0],[470,210,0],[470,214,0],[468,219,0],[468,222,0],[468,225,0],[467,227,0],[467,229,0],[467,230,0],[467,231,0],[467,232,0],[467,233,0],[467,234,0],[467,235,0],[467,236,0],[467,237,0],[467,238,0],[467,239,0],[467,240,0],[467,241,0],[467,242,0],[467,243,0],[467,244,0],[467,245,0],[467,246,0],[467,247,0],[467,249,0],[467,252,0],[467,256,0],[467,259,0],[467,261,0],[467,264,0],[467,266,0],[467,268,0],[467,269,0],[467,271,0],[467,272,0],[467,273,0],[467,275,0],[467,276,0],[467,277,0],[467,278,0],[468,279,0],[468,280,0],[468,281,0],[468,282,0],[468,283,0],[468,284,0],[468,285,0],[468,286,0],[468,288,0],[469,289,0],[469,291,0],[469,292,0],[469,293,0],[469,294,0],[470,295,0],[470,296,0],[470,297,0],[470,298,0],[470,299,0],[470,300,0],[461,154,1],[460,154,1],[459,154,1],[458,154,1],[456,154,1],[453,154,1],[450,154,1],[447,154,1],[443,154,1],[439,154,1],[434,155,1],[429,155,1],[424,156,1],[418,156,1],[412,156,1],[406,157,1],[400,157,1],[395,157,1],[390,157,1],[386,157,1],[381,157,1],[377,157,1],[372,157,1],[368,157,1],[363,157,1],[359,157,1],[355,157,1],[350,157,1],[345,157,1],[341,157,1],[337,156,1],[333,156,1],[328,156,1],[325,156,1],[322,155,1],[318,155,1],[314,154,1],[311,154,1],[309,154,1],[306,154,1],[304,154,1],[302,153,1],[300,153,1],[298,153,1],[295,153,1],[293,153,1],[291,153,1],[289,152,1],[286,152,1],[284,152,1],[281,152,1],[278,152,1],[276,152,1],[274,152,1],[272,151,1],[269,151,1],[267,151,1],[264,151,1],[262,151,1],[261,151,1],[259,151,1],[258,151,1],[256,151,1],[255,151,1],[253,151,1],[251,151,1],[249,151,1],[247,151,1],[244,151,1],[241,151,1],[239,151,1],[237,152,1],[235,152,1],[233,152,1],[231,152,1],[229,152,1],[228,153,1],[226,153,1],[225,153,1],[223,153,1],[222,153,1],[221,153,1],[220,153,1],[218,153,1],[217,153,1],[215,153,1],[214,153,1],[212,153,1],[210,153,1],[208,153,1],[207,153,1],[205,153,1],[203,153,1],[202,153,1],[201,153,1],[200,153,1],[198,153,1],[197,153,1],[196,153,1],[195,153,1],[194,153,1],[193,153,1],[191,153,1],[190,153,1],[189,153,1],[179,152,1],[178,152,1],[177,152,1],[175,152,1],[174,151,1],[173,150,1],[171,150,1],[170,150,1],[169,150,1],[167,149,1],[166,149,1],[165,149,1],[163,149,1],[162,149,1],[161,149,1],[160,149,1],[159,149,1],[158,149,1],[157,149,1],[156,149,1],[155,149,1],[154,149,1],[153,149,1],[152,149,1],[151,149,1],[150,149,1],[149,149,1],[148,149,1],[147,149,1],[146,149,1],[145,149,1],[144,149,1],[143,149,1],[142,149,1],[141,149,1],[140,149,1],[139,149,1],[139,150,1]]},{\"name\":\"T\",\"id\":\"40k23bogugq5qog2vkll1bt3ot\",\"points\":[[478,41,0],[478,42,0],[478,43,0],[478,45,0],[478,46,0],[478,50,0],[479,54,0],[479,60,0],[479,67,0],[479,76,0],[479,85,0],[479,96,0],[479,106,0],[479,117,0],[478,127,0],[477,140,0],[477,151,0],[476,161,0],[475,170,0],[474,179,0],[473,186,0],[473,193,0],[472,199,0],[471,205,0],[470,210,0],[470,214,0],[468,219,0],[468,222,0],[468,225,0],[467,227,0],[467,229,0],[467,230,0],[467,231,0],[467,232,0],[467,233,0],[467,234,0],[467,235,0],[467,236,0],[467,237,0],[467,238,0],[467,239,0],[467,240,0],[467,241,0],[467,242,0],[467,243,0],[467,244,0],[467,245,0],[467,246,0],[467,247,0],[467,249,0],[467,252,0],[467,256,0],[467,259,0],[467,261,0],[467,264,0],[467,266,0],[467,268,0],[467,269,0],[467,271,0],[467,272,0],[467,273,0],[467,275,0],[467,276,0],[467,277,0],[467,278,0],[468,279,0],[468,280,0],[468,281,0],[468,282,0],[468,283,0],[468,284,0],[468,285,0],[468,286,0],[468,288,0],[469,289,0],[469,291,0],[469,292,0],[469,293,0],[469,294,0],[470,295,0],[470,296,0],[470,297,0],[470,298,0],[470,299,0],[470,300,0],[461,154,1],[460,154,1],[459,154,1],[458,154,1],[456,154,1],[453,154,1],[450,154,1],[447,154,1],[443,154,1],[439,154,1],[434,155,1],[429,155,1],[424,156,1],[418,156,1],[412,156,1],[406,157,1],[400,157,1],[395,157,1],[390,157,1],[386,157,1],[381,157,1],[377,157,1],[372,157,1],[368,157,1],[363,157,1],[359,157,1],[355,157,1],[350,157,1],[345,157,1],[341,157,1],[337,156,1],[333,156,1],[328,156,1],[325,156,1],[322,155,1],[318,155,1],[314,154,1],[311,154,1],[309,154,1],[306,154,1],[304,154,1],[302,153,1],[300,153,1],[298,153,1],[295,153,1],[293,153,1],[291,153,1],[289,152,1],[286,152,1],[284,152,1],[281,152,1],[278,152,1],[276,152,1],[274,152,1],[272,151,1],[269,151,1],[267,151,1],[264,151,1],[262,151,1],[261,151,1],[259,151,1],[258,151,1],[256,151,1],[255,151,1],[253,151,1],[251,151,1],[249,151,1],[247,151,1],[244,151,1],[241,151,1],[239,151,1],[237,152,1],[235,152,1],[233,152,1],[231,152,1],[229,152,1],[228,153,1],[226,153,1],[225,153,1],[223,153,1],[222,153,1],[221,153,1],[220,153,1],[218,153,1],[217,153,1],[215,153,1],[214,153,1],[212,153,1],[210,153,1],[208,153,1],[207,153,1],[205,153,1],[203,153,1],[202,153,1],[201,153,1],[200,153,1],[198,153,1],[197,153,1],[196,153,1],[195,153,1],[194,153,1],[193,153,1],[191,153,1],[190,153,1],[189,153,1],[179,152,1],[178,152,1],[177,152,1],[175,152,1],[174,151,1],[173,150,1],[171,150,1],[170,150,1],[169,150,1],[167,149,1],[166,149,1],[165,149,1],[163,149,1],[162,149,1],[161,149,1],[160,149,1],[159,149,1],[158,149,1],[157,149,1],[156,149,1],[155,149,1],[154,149,1],[153,149,1],[152,149,1],[151,149,1],[150,149,1],[149,149,1],[148,149,1],[147,149,1],[146,149,1],[145,149,1],[144,149,1],[143,149,1],[142,149,1],[141,149,1],[140,149,1],[139,149,1],[139,150,1]]},{\"name\":\"T\",\"id\":\"2hhubfl0qqk30qllkf9rg1fq7p\",\"points\":[[472,41,0],[478,42,0],[478,43,0],[478,45,0],[478,46,0],[478,50,0],[479,54,0],[479,60,0],[479,67,0],[479,76,0],[479,85,0],[479,96,0],[479,106,0],[479,117,0],[478,127,0],[477,140,0],[477,151,0],[476,161,0],[475,170,0],[474,179,0],[473,186,0],[473,193,0],[472,199,0],[471,205,0],[470,210,0],[470,214,0],[468,219,0],[468,222,0],[468,225,0],[467,227,0],[467,229,0],[467,230,0],[467,231,0],[467,232,0],[467,233,0],[467,234,0],[467,235,0],[467,236,0],[467,237,0],[467,238,0],[467,239,0],[467,240,0],[467,241,0],[467,242,0],[467,243,0],[467,244,0],[467,245,0],[467,246,0],[467,247,0],[467,249,0],[467,252,0],[467,256,0],[467,259,0],[467,261,0],[467,264,0],[467,266,0],[467,268,0],[467,269,0],[467,271,0],[467,272,0],[467,273,0],[467,275,0],[467,276,0],[467,277,0],[467,278,0],[468,279,0],[468,280,0],[468,281,0],[468,282,0],[468,283,0],[468,284,0],[468,285,0],[468,286,0],[468,288,0],[469,289,0],[469,291,0],[469,292,0],[469,293,0],[469,294,0],[470,295,0],[470,296,0],[470,297,0],[470,298,0],[470,299,0],[470,300,0],[461,154,1],[460,154,1],[459,154,1],[458,154,1],[456,154,1],[453,154,1],[450,154,1],[447,154,1],[443,154,1],[439,154,1],[434,155,1],[429,155,1],[424,156,1],[418,156,1],[412,156,1],[406,157,1],[400,157,1],[395,157,1],[390,157,1],[386,157,1],[381,157,1],[377,157,1],[372,157,1],[368,157,1],[363,157,1],[359,157,1],[355,157,1],[350,157,1],[345,157,1],[341,157,1],[337,156,1],[333,156,1],[328,156,1],[325,156,1],[322,155,1],[318,155,1],[314,154,1],[311,154,1],[309,154,1],[306,154,1],[304,154,1],[302,153,1],[300,153,1],[298,153,1],[295,153,1],[293,153,1],[291,153,1],[289,152,1],[286,152,1],[284,152,1],[281,152,1],[278,152,1],[276,152,1],[274,152,1],[272,151,1],[269,151,1],[267,151,1],[264,151,1],[262,151,1],[261,151,1],[259,151,1],[258,151,1],[256,151,1],[255,151,1],[253,151,1],[251,151,1],[249,151,1],[247,151,1],[244,151,1],[241,151,1],[239,151,1],[237,152,1],[235,152,1],[233,152,1],[231,152,1],[229,152,1],[228,153,1],[226,153,1],[225,153,1],[223,153,1],[222,153,1],[221,153,1],[220,153,1],[218,153,1],[217,153,1],[215,153,1],[214,153,1],[212,153,1],[210,153,1],[208,153,1],[207,153,1],[205,153,1],[203,153,1],[202,153,1],[201,153,1],[200,153,1],[198,153,1],[197,153,1],[196,153,1],[195,153,1],[194,153,1],[193,153,1],[191,153,1],[190,153,1],[189,153,1],[179,152,1],[178,152,1],[177,152,1],[175,152,1],[174,151,1],[173,150,1],[171,150,1],[170,150,1],[169,150,1],[167,149,1],[166,149,1],[165,149,1],[163,149,1],[162,149,1],[161,149,1],[160,149,1],[159,149,1],[158,149,1],[157,149,1],[156,149,1],[155,149,1],[154,149,1],[153,149,1],[152,149,1],[151,149,1],[150,149,1],[149,149,1],[148,149,1],[147,149,1],[146,149,1],[145,149,1],[144,149,1],[143,149,1],[142,149,1],[141,149,1],[140,149,1],[139,149,1],[139,150,1]]}]"
                        ),

                },
                array = @ArraySchema(
                        schema = @Schema(ref = "#/components/schemas/gestureInfo")
                )
        )
)
@ApiEndpoint(
        path = "/gesture/list",
        userLevel = 1
)
public class GestureListServlet extends KosmoSAuthedServlet {


    public GestureListServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    public static JSONArray getGestureList(IController controller) {
        JSONArray arr = new JSONArray();
        for (Gesture g : controller.getGestureProvider().listGestures()) {
            JSONObject o = new JSONObject();
            o.put("name", g.name);
            o.put("id", g.Id);
            JSONArray pts = new JSONArray();
            for (Point p : g.rawPoints) {
                pts.put(new JSONArray().put(p.x).put(p.y).put(p.stroke));
            }
            o.put("points", pts);
            arr.put(o);

        }
        return arr;
    }

    @Operation(
            tags = {"gesture"},
            summary = "list",
            description = "Lists all known gestures",

            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK), ref = "#/components/responses/gestureList"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void get(KosmoSHttpServletRequest request, HttpServletResponse response)


            throws IOException, UnauthorizedException {


        sendJSON(request, response, getGestureList(this.controller));


    }

}

