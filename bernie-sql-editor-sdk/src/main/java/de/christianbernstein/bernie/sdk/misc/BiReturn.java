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

import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
@Data
public class BiReturn<T, V> {

    private final List<PointOfFailure> pointOfFailures;

    private final T first;

    private final V second;

    @org.jetbrains.annotations.Contract
    public static <T, V> BiReturn<T, V> wrap(T first, V second, PointOfFailure... pointOfFailures) {
        return new BiReturn<>(Arrays.asList(pointOfFailures), first, second);
    }

    public void ifFirstValuePresent(@NonNull Consumer<T> handler) {
        if (this.first != null) {
            handler.accept(this.first);
        }
    }

    public void ifSecondValuePresent(@NonNull Consumer<V> handler) {
        if (this.second != null) {
            handler.accept(this.second);
        }
    }
}
