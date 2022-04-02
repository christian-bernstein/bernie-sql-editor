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

import lombok.NonNull;

import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface ICallback<T> {

    static <T> ICallback<T> blank() {
        return data -> {
        };
    }

    static <T> ICallback<T> wrap(@NonNull Consumer<T> doneRoutine) {
        return ICallback.wrap(doneRoutine, () -> {
        });
    }

    static <T> ICallback<T> wrap(@NonNull Consumer<T> doneRoutine, @NonNull Runnable abortRoutine) {
        return new ICallback<>() {
            @Override
            public void done(T data) {
                doneRoutine.accept(data);
            }

            @Override
            public void abort() {
                abortRoutine.run();
            }
        };
    }

    void done(T data);

    default void abort() {
    }
}
