/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.sdk.misc;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@Builder
public class StateController implements Unsafe {

    @Builder.Default
    private final Map<String, Runnable> states = new HashMap<>();

    @Builder.Default
    private final IStateChangeHandler stateChangeHandler = (from, to) -> true;

    @Builder.Default
    private final Supplier<Boolean> stateNotExistsHandler = () -> true;

    private String state;

    public StateController addState(String name, Runnable action) {
        this.states.put(name, action);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public StateController callState(String state, Runnable abortHandler) {
        if (this.stateChangeHandler != null && !this.stateChangeHandler.onStateChange(this.state, state)) {
            if (abortHandler != null) {
                abortHandler.run();
            }
            return this;
        }
        final Runnable action = this.states.get(state);
        if (action != null) {
            this.unsafe(action::run);
            this.state = state;
        } else {
            if (this.stateNotExistsHandler != null && this.stateNotExistsHandler.get()) {
                // State is invalid, do not change
                this.state = state;
            }
        }
        return this;
    }

    public void callState(String state) {
        this.callState(state, () -> {
        });
    }

    public String getState() {
        return state;
    }

    @FunctionalInterface
    public interface IStateChangeHandler {

        boolean onStateChange(String from, String to);

    }
}
