package de.christianbernstein.bernie.sdk.hermes;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Server {

    boolean ssl() default false;

    int port() default 8000;

    String basePath() default "";

}

