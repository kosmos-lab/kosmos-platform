package de.kosmos_lab.kosmos.annotations.security;

import de.kosmos_lab.kosmos.annotations.enums.SecurityIn;
import de.kosmos_lab.kosmos.annotations.enums.SecurityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OAuthFlow {
    String authorizationUrl() default "";
    String tokenUrl() default "";
    String refreshUrl() default "";

    Scope[] scopes() default {};

}