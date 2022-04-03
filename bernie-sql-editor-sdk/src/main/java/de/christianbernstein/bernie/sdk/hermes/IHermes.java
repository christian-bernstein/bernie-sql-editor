package de.christianbernstein.bernie.sdk.hermes;

import java.util.concurrent.ExecutorService;

/**
 * @author Christian Bernstein
 */
public interface IHermes<Impl extends IHermes<Impl>> {

    Impl start(ExecutorService executor);
}
