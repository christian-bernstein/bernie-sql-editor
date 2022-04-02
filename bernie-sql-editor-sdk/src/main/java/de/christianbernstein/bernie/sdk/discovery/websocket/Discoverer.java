package de.christianbernstein.bernie.sdk.discovery.websocket;

import java.lang.annotation.*;

/**
 * todo add versioning to discoverer-framework
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Discoverer {

    String packetID();

    Class<? extends PacketData> datatype();

    String[] protocols() default {};

    Version version() default @Version(major = 1, minor = 0, patch = 0);

    /**
     * (Semantic versioning)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Version {

        int major();

        int minor();

        int patch();

        String[] labels() default {};

    }
}
