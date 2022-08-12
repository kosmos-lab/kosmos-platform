package de.kosmos_lab.kosmos.annotations.media;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DiscriminatorMapping {
    String value() default "";

    Class<?> schema() default Void.class;
}
