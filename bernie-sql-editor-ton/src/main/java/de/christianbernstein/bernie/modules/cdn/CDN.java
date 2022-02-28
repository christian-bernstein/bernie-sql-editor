package de.christianbernstein.bernie.modules.cdn;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CDN {

    String id();

}
