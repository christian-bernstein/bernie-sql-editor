package de.christianbernstein.bernie.ses.annotations;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Threaded {

    String pool() default "main";
}
