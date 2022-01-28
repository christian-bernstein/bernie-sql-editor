package de.christianbernstein.bernie.shared.discovery.websocket;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Discoverer {

    String packetID();

    Class<? extends PacketData> datatype();

    String[] protocols() default {};

}
