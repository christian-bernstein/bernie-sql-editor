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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christian Bernstein
 */
public interface Unsafe {

    @Deprecated
    static void staticUnsafe(@NonNull final SafeRunnable runnable) {
        try {
            runnable.run();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    default void handleException(@NonNull final Exception e) {
        e.printStackTrace();
    }

    default void unsafe(@NonNull final SafeRunnable runnable) {
        try {
            runnable.run();
        } catch (final Exception e) {
            this.handleException(e);
        }
    }

    default void tryCatch(@NonNull final SafeRunnable runnable, @NonNull final Consumer<Throwable> handler) {
        try {
            runnable.run();
        } catch (final Exception e) {
            handler.accept(e);
        }
    }

    default void specializedTryCatch(@NonNull final SafeRunnable runnable, final Consumer<Throwable> fallback, SpecifiedCatcher... catchers) {
        try {
            runnable.run();
        } catch (final Exception e) {
            final List<SpecifiedCatcher> specifiedCatchers = Stream.of(catchers).filter(specifiedCatcher -> specifiedCatcher.getSpecification().equals(e.getClass())).collect(Collectors.toList());
            if (specifiedCatchers.size() == 0) {
                if (fallback != null) {
                    fallback.accept(e);
                }
            } else {
                specifiedCatchers.forEach(specifiedCatcher -> specifiedCatcher.getHandler().accept(e));
            }
        }
    }

    default void specializedTryCatch(@NonNull final SafeRunnable runnable, SpecifiedCatcher... catchers) {
        this.specializedTryCatch(runnable, null, catchers);
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    class SpecifiedCatcher {

        @NonNull
        Class<? extends Throwable> specification;

        Consumer<Throwable> handler;
    }
}
