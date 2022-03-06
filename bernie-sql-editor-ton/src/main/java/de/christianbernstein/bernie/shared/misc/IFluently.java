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

package de.christianbernstein.bernie.shared.misc;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
public interface IFluently<T> {

    @NonNull
    T me();

    @NonNull
    default T me(@NonNull @NotNull Consumer<T> consumer) {
        final T me = this.me();
        consumer.accept(me);
        return me;
    }

    @NonNull
    default T then(@NonNull @NotNull Runnable runnable) {
        runnable.run();
        return me();
    }

    // TODO: 02.08.2021 Check if that is a good idea or not
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    default T $(@NonNull @NotNull Consumer<T> consumer) {
        return this.me(consumer);
    }

    default T doIf(@NonNull BooleanSupplier condition, Consumer<T> action) {
        return me(t -> {
            if (condition.getAsBoolean()) {
                action.accept(t);
            }
        });
    }

    default T doIf(boolean condition, Consumer<T> action) {
        return this.doIf(() -> condition, action);
    }

    default T stdout() {
        return this.me(System.out::println);
    }
}
