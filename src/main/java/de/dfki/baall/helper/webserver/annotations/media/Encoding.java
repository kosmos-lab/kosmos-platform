package de.dfki.baall.helper.webserver.annotations.media;



import de.dfki.baall.helper.webserver.annotations.headers.Header;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Encoding {
    String name() default "";

    String contentType() default "";

    String style() default "";

    boolean explode() default false;

    boolean allowReserved() default false;

    Header[] headers() default {};

}