package de.dfki.baall.helper.webserver.doc.openapi;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ApiEndpoint {

    String path();
    int userLevel() default -1;
    boolean hidden() default false;

}