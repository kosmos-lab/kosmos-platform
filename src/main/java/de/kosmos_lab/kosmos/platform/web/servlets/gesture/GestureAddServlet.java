package de.kosmos_lab.kosmos.platform.web.servlets.gesture;

import de.kosmos_lab.kosmos.annotations.Operation;
import de.kosmos_lab.kosmos.annotations.enums.SchemaType;
import de.kosmos_lab.kosmos.annotations.media.ArraySchema;
import de.kosmos_lab.kosmos.annotations.media.Content;
import de.kosmos_lab.kosmos.annotations.media.ExampleObject;
import de.kosmos_lab.kosmos.annotations.media.ObjectSchema;
import de.kosmos_lab.kosmos.annotations.media.Schema;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperty;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.doc.openapi.ApiEndpoint;
import de.kosmos_lab.kosmos.doc.openapi.ResponseCode;
import de.kosmos_lab.kosmos.exceptions.GestureAlreadyExistsException;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import de.kosmos_lab.kosmos.platform.web.servlets.AuthedServlet;
import de.kosmos_lab.kosmos.platform.web.servlets.KosmoSServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;


@ApiEndpoint(
        path = "/gesture/add",
        userLevel = 1

)

public class GestureAddServlet extends AuthedServlet {
    public GestureAddServlet(WebServer webServer, IController controller, int level) {
        super(webServer, controller, level);
    }
    
    @Operation(
            tags = {"gesture"},
            summary = "add",
            description = "Add a gesture to the system, if multiple with this name already exist they will be merged (you SHOULD always have multiple examples for a gesture).",
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schemaProperties = {
                                            
                                            @SchemaProperty(
                                                    name = "name",
                                                    schema = @Schema(
                                                            description = "The name to use for the new gesture",
                                                            type = SchemaType.STRING,
                                                            minLength = 1,
                                                            required = true
                                                    )
                                            ),
                                            @SchemaProperty(
                                                    name = "points",
                                                    array = @ArraySchema(
                                                            arraySchema = @Schema(
                                                                    description = "List of strokes this gesture consists of",
                                                                    required = true,
                                                                    ref = "#/components/schemas/pointList"
                                                            )
                                                    )
                                            )
                                    }, examples = {
                                    @ExampleObject(
                                            name = "T",
                                            value = "{\"name\":\"T\",\"points\":[[[478,41],[478,42],[478,43],[478,45],[478,46],[478,50],[479,54],[479,60],[479,67],[479,76],[479,85],[479,96],[479,106],[479,117],[478,127],[477,140],[477,151],[476,161],[475,170],[474,179],[473,186],[473,193],[472,199],[471,205],[470,210],[470,214],[468,219],[468,222],[468,225],[467,227],[467,229],[467,230],[467,231],[467,232],[467,233],[467,234],[467,235],[467,236],[467,237],[467,238],[467,239],[467,240],[467,241],[467,242],[467,243],[467,244],[467,245],[467,246],[467,247],[467,249],[467,252],[467,256],[467,259],[467,261],[467,264],[467,266],[467,268],[467,269],[467,271],[467,272],[467,273],[467,275],[467,276],[467,277],[467,278],[468,279],[468,280],[468,281],[468,282],[468,283],[468,284],[468,285],[468,286],[468,288],[469,289],[469,291],[469,292],[469,293],[469,294],[470,295],[470,296],[470,297],[470,298],[470,299],[470,300]],[[461,154],[460,154],[459,154],[458,154],[456,154],[453,154],[450,154],[447,154],[443,154],[439,154],[434,155],[429,155],[424,156],[418,156],[412,156],[406,157],[400,157],[395,157],[390,157],[386,157],[381,157],[377,157],[372,157],[368,157],[363,157],[359,157],[355,157],[350,157],[345,157],[341,157],[337,156],[333,156],[328,156],[325,156],[322,155],[318,155],[314,154],[311,154],[309,154],[306,154],[304,154],[302,153],[300,153],[298,153],[295,153],[293,153],[291,153],[289,152],[286,152],[284,152],[281,152],[278,152],[276,152],[274,152],[272,151],[269,151],[267,151],[264,151],[262,151],[261,151],[259,151],[258,151],[256,151],[255,151],[253,151],[251,151],[249,151],[247,151],[244,151],[241,151],[239,151],[237,152],[235,152],[233,152],[231,152],[229,152],[228,153],[226,153],[225,153],[223,153],[222,153],[221,153],[220,153],[218,153],[217,153],[215,153],[214,153],[212,153],[210,153],[208,153],[207,153],[205,153],[203,153],[202,153],[201,153],[200,153],[198,153],[197,153],[196,153],[195,153],[194,153],[193,153],[191,153],[190,153],[189,153],[179,152],[178,152],[177,152],[175,152],[174,151],[173,150],[171,150],[170,150],[169,150],[167,149],[166,149],[165,149],[163,149],[162,149],[161,149],[160,149],[159,149],[158,149],[157,149],[156,149],[155,149],[154,149],[153,149],[152,149],[151,149],[150,149],[149,149],[148,149],[147,149],[146,149],[145,149],[144,149],[143,149],[142,149],[141,149],[140,149],[139,149],[139,150]]]}"
                                    ),
                                    @ExampleObject(
                                            name = "X",
                                            value = "{\"name\":\"X\",\"points\":[[[368,70],[367,70],[366,71],[365,73],[362,76],[357,81],[353,85],[348,89],[343,93],[339,97],[334,102],[330,105],[325,108],[321,111],[317,115],[313,119],[309,122],[305,126],[300,130],[295,135],[290,139],[284,145],[279,150],[273,155],[268,160],[261,165],[254,171],[248,176],[242,181],[235,186],[229,192],[224,196],[219,200],[214,204],[210,209],[206,213],[202,217],[199,220],[196,224],[193,227],[190,230],[186,234],[183,238],[180,241],[177,245],[174,248],[171,251],[169,254],[166,256],[165,259],[163,260],[162,261],[160,263],[159,264],[158,265],[157,266],[156,267],[155,268],[154,268],[153,269],[153,270],[152,270],[152,271],[151,271],[151,270]],[[170,101],[171,101],[172,101],[175,103],[179,105],[184,108],[189,112],[196,117],[205,122],[213,129],[222,136],[235,145],[245,152],[254,159],[263,167],[271,174],[279,181],[287,187],[294,194],[303,201],[307,204],[314,209],[320,213],[325,217],[330,221],[335,224],[338,226],[342,229],[345,232],[348,233],[350,235],[352,237],[354,238],[356,240],[357,241],[357,242],[358,242],[358,243],[359,244],[359,245],[360,245],[361,246],[361,247]]]}"
                                    ),
                                    @ExampleObject(
                                            name = "sun",
                                            value = "{\"name\":\"X\",\"points\":[[[349,221],[349,220],[349,219],[349,218],[349,217],[349,216],[349,214],[349,213],[349,212],[349,210],[349,208],[349,206],[349,204],[349,200],[349,197],[349,194],[349,190],[349,186],[349,182],[349,178],[348,175],[347,171],[347,168],[347,165],[346,162],[346,159],[345,157],[345,155],[344,153],[344,152],[343,150],[342,149],[342,148],[342,147],[341,146],[341,145],[340,145],[339,145],[338,145],[337,145],[336,145],[335,145],[334,145],[333,145],[332,145],[331,145],[330,145],[329,146],[329,147],[328,147],[327,148],[326,149],[325,149],[324,151],[322,152],[321,153],[320,155],[318,157],[317,158],[316,160],[315,161],[314,163],[313,165],[312,166],[312,168],[311,170],[310,172],[310,173],[310,175],[310,176],[309,177],[309,178],[309,180],[309,181],[309,183],[309,185],[309,187],[309,189],[309,190],[309,193],[309,195],[309,197],[311,199],[311,201],[312,203],[312,204],[313,206],[314,207],[315,209],[316,210],[317,211],[317,213],[319,214],[320,215],[321,217],[322,218],[323,218],[329,223],[330,224],[331,225],[332,226],[333,227],[334,227],[334,228],[335,228],[336,228],[337,228],[337,229],[338,229],[339,229],[340,229],[340,228],[341,228],[341,227],[342,226],[343,225],[343,224],[344,224],[344,223],[345,223],[345,222],[345,221],[345,220],[345,219],[346,219],[346,218]],[[427,114],[427,115],[427,116],[425,117],[423,118],[420,119],[418,121],[414,123],[410,126],[406,128],[402,130],[396,134],[392,137],[389,139],[385,142],[382,144],[380,147],[378,149],[376,151],[374,153],[373,155],[372,157],[372,158],[371,160],[370,161],[370,162],[370,163],[370,164],[370,165],[370,166]],[[409,249],[407,249],[406,249],[404,248],[403,248],[400,246],[398,245],[396,243],[394,242],[392,240],[390,237],[388,235],[385,232],[383,228],[381,225],[379,222],[377,219],[376,216],[375,213],[374,212],[374,210],[373,209],[373,208]],[[258,263],[259,263],[260,263],[262,262],[265,260],[269,259],[273,258],[276,255],[282,253],[286,251],[289,248],[293,245],[297,243],[300,240],[304,237],[307,235],[309,232],[311,230],[313,228],[314,227],[315,226],[315,225],[316,224],[316,223]],[[242,155],[243,155],[245,154],[247,154],[249,154],[253,154],[257,154],[261,154],[265,155],[269,157],[273,159],[276,161],[280,163],[283,165],[287,167],[290,169],[292,171],[294,173],[297,175],[298,177],[299,178],[300,179],[301,179],[302,179]],[[317,104],[318,104],[319,106],[320,106],[320,108],[321,111],[323,114],[325,119],[327,125],[330,133],[333,141],[337,149],[340,157],[342,166],[345,173],[348,181],[349,187],[351,193],[353,198],[354,202],[355,206],[355,208],[355,209]]]}"
                                    )
                            }
                            
                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_OK), description = "The gesture was added successfully", ref = "#/components/responses/gestureList"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_CONFLICT), description = "There is already a gesture with those exact points"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_MISSING_VALUE), ref = "#/components/responses/MissingValuesError"),
                    //@ApiResponse(responseCode = @ResponseCode(statusCode = KosmoSServlet.STATUS_NO_AUTH), ref = "#/components/responses/NoAuthError"),
            })
    public void post(KosmoSHttpServletRequest request, HttpServletResponse response)
            
            throws IOException, GestureAlreadyExistsException {
        JSONObject body = request.getBodyAsJSONObject();
        
        if (controller.getGestureProvider().addGesture(
                body.getString("name")
                , body.getJSONArray("points"),
                true)) {
            sendJSON(request, response, GestureListServlet.getGestureList(this.controller));
            return;
        }
        throw new GestureAlreadyExistsException(body.getString("name"));
        
    }
    
}

