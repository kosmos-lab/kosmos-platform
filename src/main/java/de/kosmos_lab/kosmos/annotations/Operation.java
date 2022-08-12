package de.kosmos_lab.kosmos.annotations;



import de.kosmos_lab.kosmos.annotations.ExternalDocumentation;
import de.kosmos_lab.kosmos.annotations.Parameter;
import de.kosmos_lab.kosmos.annotations.extensions.Extension;
import de.kosmos_lab.kosmos.annotations.parameters.RequestBody;
import de.kosmos_lab.kosmos.annotations.responses.ApiResponse;
import de.kosmos_lab.kosmos.annotations.security.SecurityRequirement;
import de.kosmos_lab.kosmos.annotations.servers.Server;

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
