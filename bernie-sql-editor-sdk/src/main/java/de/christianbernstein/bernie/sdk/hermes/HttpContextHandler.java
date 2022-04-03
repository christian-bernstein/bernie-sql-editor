package de.christianbernstein.bernie.sdk.hermes;

import lombok.NonNull;

import java.io.IOException;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface HttpContextHandler {

    void handle(@NonNull HttpRequest request) throws IOException;

}
