package de.christianbernstein.bernie.ses.bin;

import lombok.Builder;
import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class ConsoleCommandRegisterRequest {

    private final Class<?> target;

    private final String gloriaInstance;

    private final boolean autoInstanceInvoking;

}
