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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
public interface ILink<T> extends Supplier<T> {

    default boolean determine(@NotNull Predicate<T> predicate) {
        return predicate.test(this.get());
    }

    default ILink<T> modify(@NonNull Function<T, T> modifier) {
        return this;
    }

    default boolean shouldRegenerate() {
        return false;
    }

    class Static<T> implements ILink<T> {

        private final Supplier<T> supplier;

        private T cache = null;

        public Static(@NonNull final Supplier<T> supplier) {
            this.supplier = supplier;
            this.cache = this.supplier.get();
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public static <T> Static<T> of(@NonNull Supplier<T> supplier) {
            return new Static<>(supplier);
        }

        @Override
        public ILink<T> modify(@NonNull Function<T, T> modifier) {
            this.cache = modifier.apply(this.get());
            return this;
        }

        @Override
        public T get() {
            if (this.shouldRegenerate()) {
                this.cache = this.supplier.get();
                return this.cache;
            }
            return this.cache;
        }
    }
}
