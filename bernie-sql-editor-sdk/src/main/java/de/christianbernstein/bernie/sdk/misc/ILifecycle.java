package de.christianbernstein.bernie.sdk.misc;

import lombok.NonNull;

public interface ILifecycle {

    default void start() {
    }

    default void stop() {
    }

    @NonNull
    default State getLifecycleState() {
        return State.UNKNOWN;
    }

    enum State {
        STARTING, STARTED, STOPPING, STOPPED, UNKNOWN
    }
}
