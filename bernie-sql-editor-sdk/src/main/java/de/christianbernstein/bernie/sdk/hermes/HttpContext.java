package de.christianbernstein.bernie.sdk.hermes;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpContext {

    String value();

}
