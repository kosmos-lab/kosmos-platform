package de.dfki.baall.helper.webserver.annotations;



import de.dfki.baall.helper.webserver.annotations.ExternalDocumentation;
import de.dfki.baall.helper.webserver.annotations.Parameter;
import de.dfki.baall.helper.webserver.annotations.extensions.Extension;
import de.dfki.baall.helper.webserver.annotations.parameters.RequestBody;
import de.dfki.baall.helper.webserver.annotations.responses.ApiResponse;
import de.dfki.baall.helper.webserver.annotations.security.SecurityRequirement;
import de.dfki.baall.helper.webserver.annotations.servers.Server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface Operation {

    String method() default "";

    String[] tags() default {};

    String summary() default "";

    String description() default "";

    RequestBody requestBody() default @RequestBody;

    ExternalDocumentation externalDocs() default @ExternalDocumentation;

    String operationId() default "";

    Parameter[] parameters() default {};

    ApiResponse[] responses() default {};

    boolean deprecated() default false;

    SecurityRequirement[] security() default {};

    Server[] servers() default {};

    Extension[] extensions() default {};

    boolean hidden() default false;

    boolean ignoreJsonView() default false;


}
