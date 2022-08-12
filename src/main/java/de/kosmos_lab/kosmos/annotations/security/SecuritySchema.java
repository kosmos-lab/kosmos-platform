package de.kosmos_lab.kosmos.annotations.security;

import de.kosmos_lab.kosmos.annotations.enums.ParameterIn;
import de.kosmos_lab.kosmos.annotations.enums.SecurityIn;
import de.kosmos_lab.kosmos.annotations.enums.SecurityType;
import de.kosmos_lab.kosmos.annotations.media.SchemaProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SecuritySchemas.class)

public @interface SecuritySchema {
    String componentName() default "";
    String name() default "";
    SecurityType type();
    String description() default "";
    SecurityIn in() default SecurityIn.NONE;
    String scheme() default "";
    String bearerFormat() default "";
    OAuthFlows flows() default @OAuthFlows();
    String openIdConnectUrl() default "";
}