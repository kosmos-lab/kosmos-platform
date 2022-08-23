package de.dfki.baall.helper.webserver.doc.openapi;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ApiComponent {

    public String description() default "";
    public String type();
    public String name();

}