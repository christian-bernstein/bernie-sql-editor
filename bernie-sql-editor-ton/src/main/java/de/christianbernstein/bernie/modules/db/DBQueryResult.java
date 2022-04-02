package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.sdk.misc.SerializedException;
import lombok.Builder;
import lombok.Data;

import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class DBQueryResult {

    private boolean success;

    private List<SerializedException> exceptions;

    /**
     * epd is short for engine processing duration
     */
    private Duration epd;

    private ResultSet result;
}
