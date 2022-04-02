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
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IBindable<V extends IBindable<V, T>, T> {

    boolean isBinding();

    @NonNull
    V bind(@NonNull T object);

    @NonNull
    V unbind();

    T getBoundedObject();

    @NonNull
    V ifBinding(Consumer<V> handler);

    @NonNull
    V ifNotBinding(Consumer<V> handler);

    @NonNull
    V executePrioritizedWith(T temp, Consumer<V> handler);

    @NonNull
    V bindIfThisIsNull(T object);

    @NonNull
    V __getInstance();

    interface Default<V extends IBindable<V, T>, T> extends IBindable<V, T> {

        @Override
        default boolean isBinding() {
            return this.getBoundedObject() != null;
        }

        @NotNull
        @Override
        default V ifBinding(Consumer<V> handler) {
            if (this.isBinding()) {
                if (handler != null) {
                    handler.accept(this.__getInstance());
                }
            }
            return this.__getInstance();
        }

        @NotNull
        @Override
        default V ifNotBinding(Consumer<V> handler) {
            if (!this.isBinding()) {
                if (handler != null) {
                    handler.accept(this.__getInstance());
                }
            }
            return this.__getInstance();
        }

        @NotNull
        @Override
        default V bindIfThisIsNull(T object) {
            this.ifNotBinding(tiBindable -> tiBindable.bind(object));
            return this.__getInstance();
        }

        @NotNull
        @Override
        default V executePrioritizedWith(T temp, Consumer<V> handler) {
            if (temp != null) {
                final T saved = this.getBoundedObject();
                this.unbind();
                this.bind(temp);
                handler.accept(this.__getInstance());
                this.unbind();
                this.bind(saved);
            } else {
                handler.accept(this.__getInstance());
            }
            return this.__getInstance();
        }
    }
}
