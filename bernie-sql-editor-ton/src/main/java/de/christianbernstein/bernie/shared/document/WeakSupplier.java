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

package de.christianbernstein.bernie.shared.document;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@AllArgsConstructor
public
class WeakSupplier<V, T> implements Supplier<T> {

    @NonNull
    private final V document;

    @NonNull
    private final Function<V, T> method;

    public static <V, T> WeakSupplier<V, T> of(V document, Function<V, T> method) {
        return new WeakSupplier<>(document, method);
    }

    public T get() {
        return this.method.apply(this.document);
    }
}
