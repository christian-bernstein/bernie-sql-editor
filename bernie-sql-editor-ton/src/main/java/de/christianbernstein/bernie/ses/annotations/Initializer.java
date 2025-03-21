package de.christianbernstein.bernie.ses.annotations;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Initializer {
}
