package de.kosmos_lab.platform.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.kosmos_lab.platform.data.KosmoSUser;
import de.kosmos_lab.web.annotations.Operation;
import de.kosmos_lab.web.annotations.Parameter;
import de.kosmos_lab.web.annotations.enums.ParameterIn;
import de.kosmos_lab.web.annotations.headers.Header;
import de.kosmos_lab.web.annotations.media.ExampleObject;
import de.kosmos_lab.web.annotations.media.Schema;
import de.kosmos_lab.web.annotations.responses.ApiResponse;
import de.kosmos_lab.web.annotations.security.SecurityRequirement;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;

import java.io.IOException;


@Deprecated
public class OpenApiToYaml {
    public static String asYaml(String jsonString) throws IOException {
        // parse JSON
        JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
        // save it as YAML
        String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        return jsonAsYaml;
    }

    public static String toYaml(ApiEndpoint endpoint, Operation method, String httpMethod) {
        return toYaml(endpoint, method, 0, httpMethod);
    }

    public static String toYaml(ApiEndpoint endpoint, Operation method, int tabs, String httpMethod) {
        StringBuilder sb = new StringBuilder();
        append(endpoint, method, tabs, httpMethod, sb);
        return sb.toString();
    }

    public static void append(ApiEndpoint endpoint, Operation method, int tabs, String httpMethod, StringBuilder sb) {
        if (endpoint.path() != "") {
            appendItem(endpoint.path(), tabs, sb);
            appendItem(httpMethod, tabs + 2, sb);

//            sb.append(String.format("%s%s:\n", " ".repeat(tabs), endpoint.path()));
//            sb.append(String.format("%s%s:\n", " ".repeat(tabs + 2), httpMethod));
            append("tags", method.tags(), tabs + 4, sb);
            String operationId = method.operationId();
            if (operationId.length() == 0) {
                operationId = endpoint.path().replace("/", "");
            }
            append("operationId", operationId, tabs + 4, sb);
            append("description", method.description(), tabs + 4, sb);
            append("summary", method.summary(), tabs + 4, sb);
            if (method.security().length == 0) {


                //append(method.security(), tabs + 4, sb);
                if (endpoint.userLevel() >= KosmoSUser.LEVEL_ADMIN) {
                    //append(new ApiSecurity[]{@ApiSecurity(name = "bearerAuth", scopes = {"admin"})}, tabs + 4, sb);
                    appendItem("security", tabs + 4, sb);
                    appendListItem("bearerAuth [admin]", tabs + 4, sb);
                    //method.security({@ApiSecurity(name = "bearerAuth", scopes = {"admin"})});
                } else if (endpoint.userLevel() >= 0) {
                    appendItem("security", tabs + 4, sb);
                    appendListItem("bearerAuth [user]", tabs + 4, sb);
                }
            } else {
                append(method.security(), tabs + 4, sb);
            }
            append(method.responses(), tabs + 4, sb);
            append(method.parameters(), tabs + 4, sb);

        }

    }

    public static void appendItem(String tag, int tabs, StringBuilder sb) {
        sb.append(String.format("%s%s:\n", tabs(tabs), tag));

    }

    public static void append(SecurityRequirement[] securities, int tabs, StringBuilder sb) {
        if (securities.length > 1) {

            appendItem("security", tabs, sb);
            for (SecurityRequirement s : securities) {
                append(s, tabs + 2, sb);
            }
        } else if (securities.length == 1) {
            SecurityRequirement security = securities[0];
            if (!security.name().equals("none")) {
                appendItem("security", tabs, sb);
                append(security, tabs + 2, sb);
            }
        }


    }

    public static void append(SecurityRequirement security, int tabs, StringBuilder sb) {

        if (!security.name().equals("none")) {
            sb.append(String.format("%s- %s: [%s]\n", " ".repeat(tabs), security.name(), String.join(",", security.scopes())));
        }
    }

    public static void append(Parameter r, int tabs, StringBuilder sb) {
        sb.append(String.format("%s- name: %s\n", tabs(tabs), r.name()));
        append("description", r.description(), tabs + 2, sb);

        if (r.in() != ParameterIn.DEFAULT) {
            sb.append(String.format("%sin: %s\n", " ".repeat(tabs + 2), r.in()));
        } else {
            sb.append(String.format("%sin: %s\n", " ".repeat(tabs + 2), "query"));
        }
        //sb.append(String.format("%srequired: %s\n", " ".repeat(tabs + 2), r.required()));
        append(r.schema(), tabs + 2, sb);

        append(r.examples(), tabs + 2, sb);
        append("example", r.example(), tabs + 2, sb);


    }


    private static void append(ExampleObject[] examples, int tabs, StringBuilder sb) {
        if (examples.length > 0) {
            appendItem("examples", tabs, sb);
            for (ExampleObject e : examples) {
                append(e, tabs + 2, sb);
            }
        }
    }

    public static void append(Parameter[] rs, int tabs, StringBuilder sb) {
        if (rs.length > 0) {
            //sb.append(String.format("%sparameters:\n", " ".repeat(tabs)));
            appendItem("parameters", tabs, sb);
            for (Parameter r : rs) {
                append(r, tabs + 2, sb);
            }
        }

    }

    public static void append(ApiResponse[] rs, int tabs, StringBuilder sb) {
        if (rs.length > 0) {

            appendItem("responses", tabs, sb);

            for (ApiResponse r : rs) {
                append(r, tabs + 2, sb);
            }
        }

    }

    public static void append(ApiResponse r, int tabs, StringBuilder sb) {
        //sb.append(String.format("%s'%s':\n", " ".repeat(tabs), r.responseCode()));
        appendItem(String.format("'%s'", r.responseCode()), tabs, sb);
        append("description", r.description(), tabs + 2, sb);
        append("$ref", r.ref(), tabs + 2, sb);

        append(r.headers(), tabs + 2, sb);

    }

    private static void append(Header[] headers, int tabs, StringBuilder sb) {
        for (Header header : headers) {
            append(header, tabs + 2, sb);
        }
    }

    private static void append(Header header, int tabs, StringBuilder sb) {

    }

    public static void append(Schema schema, int tabs, StringBuilder sb) {
        appendItem("schema", tabs, sb);
        append("$ref", schema.ref(), tabs + 2, sb);
        append("additionalProperties", schema.additionalProperties(), tabs + 2, sb);
        append("allOf", schema.allOf(), tabs + 2, sb);
        append("allowableValues", schema.allowableValues(), tabs + 2, sb);
        append("anyOf", schema.anyOf(), tabs + 2, sb);
        append("defaultValue", schema.defaultValue(), tabs + 2, sb);
        append("deprecated", schema.deprecated(), tabs + 2, sb, false);
        append("discriminatorProperty", schema.discriminatorProperty(), tabs + 2, sb);
        append("enumAsRef", schema.enumAsRef(), tabs + 2, sb, false);
        append("exclusiveMaximum", schema.exclusiveMaximum(), tabs + 2, sb, false);
        append("exclusiveMinimum", schema.exclusiveMinimum(), tabs + 2, sb, false);

        append("format", schema.format(), tabs + 2, sb);
        append("hidden", schema.hidden(), tabs + 2, sb, false);
        append("maxLength", schema.maxLength(), tabs + 2, sb, Integer.MAX_VALUE);
        append("maxProperties", schema.maxProperties(), tabs + 2, sb, 0.0);
        append("maximum", schema.maximum(), tabs + 2, sb);
        append("minLength", schema.minLength(), tabs + 2, sb, 0);
        append("minProperties", schema.minProperties(), tabs + 2, sb, 0);
        append("minimum", schema.minimum(), tabs + 2, sb);
        append("multipleOf", schema.multipleOf(), tabs + 2, sb, 0.0);
        append("nullable", schema.nullable(), tabs + 2, sb, false);
        append("oneOf", schema.oneOf(), tabs + 2, sb);
        append("pattern", schema.pattern(), tabs + 2, sb);
        append("required", schema.required(), tabs + 2, sb, false);
        append("requiredProperties", schema.requiredProperties(), tabs + 2, sb);
        append("title", schema.title(), tabs + 2, sb);
        append("type", schema.type().toString(), tabs + 2, sb);
        append("not", schema.not(), tabs + 2, sb, Void.class);

    }

    private static void append(String tag, Class<?> clz, int tabs, StringBuilder sb, Class<?> ignoreIfThis) {
        if (ignoreIfThis != null && ignoreIfThis == clz) {
            return;

        }
        appendItem("not", tabs, sb);
        append("type", clz.getName(), tabs + 2, sb);

    }

    private static void append(String tag, int value, int tabs, StringBuilder sb, Integer ignoreIfThis) {
        if (ignoreIfThis != null && ignoreIfThis == value) {
            return;
        }
        append(tag, String.valueOf(value), tabs, sb);
    }

    private static void append(String tag, boolean input, int tabs, StringBuilder sb) {
        append(tag, input, tabs, sb, null);
    }

    private static void append(String tag, Class<?>[] classList, int tabs, StringBuilder sb) {
        if (classList.length > 0) {
            appendItem(tag, tabs, sb);
            for (Class T : classList) {
                appendListItem(T.getName(), tabs + 2, sb);
            }
        }

    }

    private static void appendListItem(String name, int tabs, StringBuilder sb) {
        sb.append(String.format("%s- %s\n", tabs(tabs + 2), name));

    }


    private static void append(String tag, double value, int tabs, StringBuilder sb, Double ignoreIfThis) {
        if (ignoreIfThis != null && ignoreIfThis == value) {
            return;
        }
        append(tag, String.valueOf(value), tabs, sb);

    }

    public static void append(ExampleObject example, int tabs, StringBuilder sb) {


        if (example.name().length() > 0) {
            appendItem(example.name(), tabs, sb);
            tabs += 2;
            append("description", example.description(), tabs, sb);
            append("summary", example.summary(), tabs, sb);
            append("$ref", example.ref(), tabs, sb);
            append("value", example.value(), tabs, sb);

        }

    }

    public static void appendTabbed(String input, int tabs, StringBuilder sb) {
        sb.append(String.format("%s%s\n", tabs(tabs), input));
    }

    public static void append(String tag, boolean input, int tabs, StringBuilder sb, Boolean ignoreIfThis) {
        if (ignoreIfThis != null && ignoreIfThis == input) {
            return;
        }
        sb.append(String.format("%s%s: %s\n", tabs(tabs), tag, input));


    }

    public static String tabs(int tabs) {
        return " ".repeat(tabs);
    }

    public static void append(String tag, String input, int tabs, StringBuilder sb) {
        if (input == null || input.length() == 0) {
            return;
        }
        String[] parts = input.split("\n");
        if (parts.length == 1) {
            //append(input,tag,tabs,sb);
            sb.append(String.format("%s%s: %s\n", tabs(tabs), tag, input));
        } else {

            sb.append(String.format("%s%s: |\n", tabs(tabs), tag, input));
            for (String part : parts) {
                sb.append(String.format("%s%s\n", " ".repeat(tabs + 2), part));
            }
        }
    }

    public static void append(String tag, String[] input, int tabs, StringBuilder sb) {
        if (input == null || input.length == 0) {
            return;
        }
        sb.append(String.format("%s%s:\n", tabs(tabs), tag));
        for (String string : input) {
            //sb.append(String.format("%s- %s\n", " ".repeat(tabs + 2), string));
            appendListItem(string, tabs + 2, sb);
        }

    }
}
