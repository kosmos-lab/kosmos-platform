package de.kosmos_lab.kosmos.annotations.info;

import de.kosmos_lab.kosmos.annotations.extensions.Extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Info {
    /**
     * The title of the application.
     *
     * @return the application's title
     **/
    String title() default "";

    /**
     * A short description of the application. CommonMark syntax can be used for rich text representation.
     *
     * @return the application's description
     **/
    String description() default "";

    /**
     * A URL to the Terms of Service for the API. Must be in the format of a URL.
     *
     * @return the application's terms of service
     **/
    String termsOfService() default "";

    /**
     * The contact information for the exposed API.
     *
     * @return a contact for the application
     **/
    Contact contact() default @Contact();

    /**
     * The license information for the exposed API.
     *
     * @return the license of the application
     **/
    License license() default @License();

    /**
     * The version of the API definition.
     *
     * @return the application's version
     **/
    String version() default "";

    /**
     * The list of optional extensions
     *
     * @return an optional array of extensions
     */
    Extension[] extensions() default {};

}