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

package de.christianbernstein.bernie.sdk.document;

import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

/**
 * @author Christian Bernstein
 */
@Data
public
class Shared<V extends IDocument<V>, T> {

    @NonNull
    private final V document;

    private final IGetter<V, T> getter;

    private final ISetter<V, T> setter;

    public static <V extends IDocument<V>, T> Shared<V, T> of(V document, IGetter<V, T> getter, ISetter<V, T> setter) {
        return new Shared<>(document, getter, setter);
    }

    public T get() {
        if (this.getter == null) {
            return null;
        }
        return this.getter.get(this.document);
    }

    public V set(T newValue) {
        if (this.setter == null) {
            return this.document;
        }
        return this.setter.set(this.document, newValue);
    }

    public void update(@NotNull UnaryOperator<T> updater) {
        this.set(updater.apply(this.get()));
    }

    // todo implement
    @FunctionalInterface
    public interface ISetter<T extends IDocument<T>, V> {

        T set(T holder, V value);
    }


    // todo implement
    @FunctionalInterface
    public interface IGetter<T extends IDocument<T>, V> {

        V get(T holder);
    }
}
