package de.christianbernstein.bernie.ses.cdn;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CDN {

    String id();

}
