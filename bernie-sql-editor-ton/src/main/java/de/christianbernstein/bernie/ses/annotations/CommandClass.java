package de.christianbernstein.bernie.ses.annotations;

import de.christianbernstein.bernie.ses.bin.Constants;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandClass {

    boolean autoInstanceInvoking() default true;

    String[] gloriaInstances() default {
            Constants.mainGloriaInstanceID
    };
}
