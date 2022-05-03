package de.christianbernstein.bernie.modules.config;

import java.lang.annotation.*;

/**
 * @author Christian Bernstein
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigDeclaration {

    String name();

}
