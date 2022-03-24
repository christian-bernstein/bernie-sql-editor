package de.christianbernstein.bernie.ses.bin;

import ch.qos.logback.classic.Level;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class LoggerConfig {

    private String logger;

    private LoggerSpecificationType type;

    private String level;

}
