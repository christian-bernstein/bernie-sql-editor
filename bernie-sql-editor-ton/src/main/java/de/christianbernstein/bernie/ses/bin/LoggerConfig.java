package de.christianbernstein.bernie.ses.bin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggerConfig {

    private String logger;

    private LoggerSpecificationType type;

    private String level;

}
