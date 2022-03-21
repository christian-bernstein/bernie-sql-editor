package de.christianbernstein.bernie.ses.bin;

import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface IMain<Impl extends ITon> {

    MainResult main(@NonNull Impl ton);

}
