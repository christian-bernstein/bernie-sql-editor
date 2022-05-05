package de.christianbernstein.bernie.ses.bin;

import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface FanoutProcedure {

    void run(@NonNull ITon ton) throws Exception;

}
