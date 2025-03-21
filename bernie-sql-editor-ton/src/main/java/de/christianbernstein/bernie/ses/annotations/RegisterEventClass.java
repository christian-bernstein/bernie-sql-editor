package de.christianbernstein.bernie.ses.annotations;

import com.google.common.annotations.Beta;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface RegisterEventClass {

}
