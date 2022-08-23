package de.dfki.baall.helper.webserver.annotations;



import de.dfki.baall.helper.webserver.annotations.enums.Explode;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterIn;
import de.dfki.baall.helper.webserver.annotations.enums.ParameterStyle;
import de.dfki.baall.helper.webserver.annotations.extensions.Extension;
import de.dfki.baall.helper.webserver.annotations.media.ArraySchema;
import de.dfki.baall.helper.webserver.annotations.media.Content;
import de.dfki.baall.helper.webserver.annotations.media.ExampleObject;
import de.dfki.baall.helper.webserver.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER,ElementType.LOCAL_VARIABLE , ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Parameters.class)
@Inherited
public @interface Parameter {
    String componentName() default "";
    String name() default "";

    ParameterIn in() ;

    String description() default "";

    boolean required() default false;

    boolean deprecated() default false;

    boolean allowEmptyValue() default false;

    ParameterStyle style() default ParameterStyle.DEFAULT;

    Explode explode() default Explode.DEFAULT;

    boolean allowReserved() default false;

    Schema schema() default @Schema;

    ArraySchema array() default @ArraySchema;

    Content[] content() default {};

    boolean hidden() default false;

    ExampleObject[] examples() default {};

    String example() default "";

    Extension[] extensions() default {};

    String ref() default "";
}
