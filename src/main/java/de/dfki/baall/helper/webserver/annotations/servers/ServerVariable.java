package de.dfki.baall.helper.webserver.annotations.servers;


import de.dfki.baall.helper.webserver.annotations.extensions.Extension;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ServerVariable {
    String name();

    String[] allowableValues() default {""};

    String defaultValue();

    String description() default "";

    Extension[] extensions() default {};
}
