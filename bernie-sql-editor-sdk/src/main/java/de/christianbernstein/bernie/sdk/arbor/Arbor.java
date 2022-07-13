package de.christianbernstein.bernie.sdk.arbor;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Arbor {

    String channel();

}
