package de.christianbernstein.bernie.sdk.arbor;

import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
public class Test {

    @Arbor(channel = "main")
    private static final IArborRunner main = (args) -> {

    };

    @FunctionalInterface
    public interface IArborRunner {
        void run(@NonNull String[] args);
    }
}
