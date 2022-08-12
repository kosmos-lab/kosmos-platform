package de.kosmos_lab.kosmos.annotations.headers;


import de.kosmos_lab.kosmos.annotations.media.Schema;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Header {
    String name();

    String description() default "";

    Schema schema() default @Schema;

    boolean required() default false;

    boolean deprecated() default false;

    String ref() default "";
}
