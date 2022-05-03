package de.christianbernstein.bernie.modules.config;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Element {

    String name();

}
