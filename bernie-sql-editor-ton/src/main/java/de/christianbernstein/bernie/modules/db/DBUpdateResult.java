package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.shared.misc.SerializedException;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class DBUpdateResult {

    private boolean success;

    private List<SerializedException> exceptions;

    /**
     * epd is short for engine processing duration
     */
    private Duration epd;

    private int code;

    private int affectedRows;
}
