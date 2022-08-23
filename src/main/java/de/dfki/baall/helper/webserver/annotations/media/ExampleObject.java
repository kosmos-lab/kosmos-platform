package de.dfki.baall.helper.webserver.annotations.media;


import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExampleObject {
    String name() default "";
    String summary() default "";
    String value() default "";

    String externalValue() default "";
    String ref() default "";
    String description() default "";
}