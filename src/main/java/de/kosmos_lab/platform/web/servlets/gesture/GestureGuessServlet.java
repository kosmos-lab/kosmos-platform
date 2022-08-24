package de.kosmos_lab.platform.web.servlets.gesture;

import de.kosmos_lab.platform.gesture.data.Gesture;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.enums.SchemaType;
import de.kosmos_lab.web.annotations.media.ArraySchema;
import de.kosmos_lab.web.annotations.media.Content;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.media.SchemaProperty;
import de.kosmos_lab.web.annotations.parameters.RequestBody;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;
import de.kosmos_lab.web.doc.openapi.ResponseCode;
import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.platform.web.servlets.KosmoSAuthedServlet;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ApiEndpoint(
        path = "/gesture/guess",
        userLevel = 1
)
public class GestureGuessServlet extends KosmoSAuthedServlet {


    public GestureGuessServlet(KosmoSWebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }

    @Operation(
            tags = {"gesture"},
            summary = "guess",
            description = "Guess the gesture based on whats known to the system",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    array = @ArraySchema(
                                            arraySchema = @Schema(ref="#/components/schemas/pointList")
                                    ),
                                     examples = {
                                    @ExampleObject(
                                            name = "T",
                                            value = "[[[475,315],[475,314],[475,313],[475,312],[475,310],[475,306],[476,303],[476,299],[477,294],[477,289],[478,284],[479,279],[479,274],[480,269],[481,264],[482,259],[482,254],[483,250],[483,246],[484,241],[485,236],[486,231],[486,227],[487,221],[488,217],[488,213],[488,208],[489,204],[489,201],[489,198],[489,195],[489,192],[489,189],[489,187],[489,183],[489,180],[489,176],[489,172],[489,169],[489,164],[489,161],[488,157],[488,155],[487,152],[487,148],[486,146],[486,143],[485,140],[485,138],[485,135],[484,132],[483,130],[483,128],[482,126],[482,124],[481,122],[481,120],[481,119],[481,118],[480,116],[479,115],[479,114],[479,113],[479,112],[479,111],[478,110],[478,109],[478,108],[478,107],[478,106],[477,106],[477,105],[477,104],[477,103],[477,102],[477,101],[477,100],[477,99],[477,98],[477,97],[477,96],[477,95],[477,94],[477,93],[477,92],[477,91],[477,90],[477,89]],[[449,181],[446,180],[444,180],[441,179],[436,178],[433,177],[428,176],[424,176],[419,176],[414,175],[409,174],[403,174],[396,174],[391,174],[385,174],[379,174],[373,175],[367,175],[361,176],[355,176],[349,177],[344,178],[337,178],[332,178],[327,178],[322,178],[318,178],[313,178],[310,178],[307,178],[302,177],[300,177],[296,176],[292,176],[289,175],[286,175],[283,174],[280,174],[277,174],[274,174],[271,173],[268,173],[265,173],[262,173],[259,173],[256,172],[254,172],[251,172],[250,172],[247,171],[246,171],[244,171],[242,170],[240,170],[238,170],[236,170],[235,170],[233,170],[231,169],[229,169],[227,169],[226,169],[223,169],[222,169],[220,169],[218,169],[217,169],[215,169],[213,169],[212,169],[210,169],[209,169],[207,169],[206,169],[205,168],[204,168],[203,168],[202,168],[201,168],[200,168],[199,168],[198,168]]]"
                                    ),
                                    @ExampleObject(
                                            name = "X",
                                            value = "[[[424,128],[423,128],[420,128],[417,128],[411,130],[405,131],[399,133],[391,135],[383,139],[375,143],[365,148],[355,155],[345,161],[336,167],[327,174],[319,180],[310,187],[302,194],[294,201],[287,208],[280,216],[272,222],[265,231],[259,237],[254,243],[249,248],[245,252],[241,257],[237,262],[234,266],[231,269],[228,273],[226,276],[224,278],[221,280],[219,282],[217,285],[216,286],[213,288],[212,290],[210,291],[209,292],[207,293],[206,294],[204,295],[202,296],[200,297],[199,298],[197,298],[195,299],[192,300],[191,301],[189,302],[187,302],[186,303],[185,303],[185,304],[184,304]],[[163,112],[164,112],[167,112],[170,112],[174,112],[179,114],[185,117],[191,119],[198,122],[206,126],[215,131],[225,137],[234,142],[245,150],[255,156],[266,164],[278,171],[290,179],[300,187],[310,194],[320,201],[328,207],[336,214],[343,220],[350,227],[355,232],[359,237],[362,241],[365,245],[368,248],[369,251],[372,254],[373,257],[374,260],[376,262],[377,265],[379,267],[380,269],[381,272],[382,274],[384,277],[385,279],[386,282],[388,285],[389,287],[389,289],[390,291],[391,293],[391,295],[392,296],[392,297],[392,298]]]"
                                    ),
                                    @ExampleObject(
                                            name = "sun",
                                            value = "[[[306,244],[307,244],[309,244],[310,244],[312,244],[313,244],[314,244],[315,244],[317,242],[318,241],[319,239],[320,237],[321,234],[323,231],[323,227],[325,223],[326,219],[327,214],[327,210],[327,205],[327,200],[327,196],[327,190],[327,185],[327,181],[327,177],[327,173],[326,170],[326,167],[324,165],[323,163],[323,160],[321,157],[320,156],[319,153],[317,152],[315,150],[313,148],[311,147],[309,145],[306,145],[303,144],[301,144],[298,144],[295,144],[291,144],[288,146],[284,148],[280,151],[276,154],[273,156],[269,159],[267,162],[264,164],[261,167],[259,171],[257,174],[255,176],[253,180],[252,183],[251,185],[251,188],[250,191],[249,193],[249,196],[249,198],[249,200],[249,203],[249,205],[249,208],[250,211],[251,214],[251,217],[252,220],[254,223],[255,226],[256,229],[258,232],[260,235],[262,237],[264,241],[266,243],[267,245],[269,247],[271,248],[273,250],[275,251],[277,252],[280,252],[282,252],[285,252],[288,252],[291,252],[293,252],[295,252],[297,252],[299,252],[301,251],[304,251],[305,250],[308,249],[309,248],[312,246],[314,245],[317,242],[318,241],[320,239],[321,237],[321,236],[321,235],[321,234],[321,233]],[[419,154],[418,153],[416,153],[413,152],[410,151],[407,151],[403,151],[398,151],[393,151],[388,151],[382,152],[377,155],[373,157],[369,159],[365,162],[362,165],[359,167],[357,170],[354,173],[351,176],[348,178],[346,181],[344,182],[342,184],[340,185],[339,186],[337,187],[337,188]],[[388,274],[387,274],[386,272],[385,271],[383,269],[382,267],[381,265],[379,263],[377,261],[376,259],[374,257],[373,255],[372,253],[370,251],[369,250],[367,247],[366,246],[365,244],[363,242],[362,241],[360,239],[358,237],[357,235],[356,234],[354,232],[353,230],[352,229],[350,228],[350,227],[349,226],[348,226]],[[274,296],[274,295],[274,294],[275,293],[276,292],[277,290],[279,287],[282,284],[284,281],[287,278],[289,275],[291,273],[294,271],[295,269],[296,268],[298,267],[299,266],[300,264],[301,263],[302,262],[303,261]],[[192,273],[192,272],[192,271],[192,270],[192,269],[193,267],[195,264],[197,262],[199,260],[201,258],[203,256],[206,254],[208,251],[211,250],[214,246],[217,244],[219,241],[222,239],[224,236],[226,234],[227,232],[229,231],[230,230],[231,228],[232,228],[232,227],[233,226],[233,225]],[[184,146],[185,146],[185,145],[187,145],[189,145],[193,145],[197,147],[202,148],[206,151],[210,153],[215,156],[219,158],[223,160],[226,162],[229,164],[232,166],[234,167],[236,168],[237,168],[239,170],[240,170],[241,170],[243,170],[244,170],[245,170],[245,171],[246,171],[247,171]],[[259,88],[260,88],[261,89],[263,91],[264,93],[266,97],[267,101],[270,106],[271,112],[274,118],[276,123],[278,129],[280,136],[281,141],[283,147],[284,151],[285,154],[285,157],[286,158],[286,159]],[[345,102],[344,102],[343,103],[342,105],[339,107],[338,109],[336,111],[334,114],[333,117],[331,120],[330,123],[329,126],[328,128],[327,131],[325,135],[324,138],[322,142],[321,145],[319,148],[318,151],[317,153],[315,155],[314,157],[313,158],[313,159],[312,159]]]"
                                    )
                            }

                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(
                            statusCode = de.kosmos_lab.web.server.WebServer.STATUS_OK),
                            description = "The gesture was detected successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name="result",
                                                    schema = @Schema(description = "the name of the gesture detected",type = SchemaType.STRING)
                                            ),
                                            @SchemaProperty(
                                                    name="key",
                                                    schema = @Schema(description = "the id of the gesture detected",type = SchemaType.STRING)
                                            )
                                    },
                                    examples = {
                                            @ExampleObject(name="X",value="{\"result\":\"X\",\"key\":\"5s96k4lvq07v4r28859mfjlbmq\"}"),
                                            @ExampleObject(name="T",value="{\"result\":\"T\",\"key\":\"40k23bogugq5qog2vkll1bt3ot\"}")

                                    }
                            )
                    ),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)

            throws IOException {
        Gesture g = controller.getGestureProvider().predict(request.getBodyAsJSONArray());
        if (g != null) {
            JSONObject obj = new JSONObject();

            obj.put("result", g.name);
            obj.put("key", g.Id);
            sendJSON(request, response, obj);
        }
    }

}

