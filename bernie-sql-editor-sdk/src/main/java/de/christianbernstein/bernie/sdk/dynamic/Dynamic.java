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

package de.christianbernstein.bernie.sdk.dynamic;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Dynamic<T, ID> implements IDynamic<T, ID> {

    @NonNull
    private final Map<ID, Supplier<T>> options = new HashMap<>();

    @NonNull
    @Getter
    @Accessors(fluent = true)
    private ID recommendedID;

    public Dynamic(@NonNull final ID recommendedID) {
        this.recommendedID = recommendedID;
    }

    @NotNull
    @Override
    public IDynamic<T, ID> recommendedID(@NotNull final ID newRecommendedID) {
        this.recommendedID = newRecommendedID;
        return this;
    }

    @Override
    public T getRecommended() {
        return this.get(this.recommendedID);
    }

    @Override
    public T get(@NotNull final ID id) {
        return this.options.get(id).get();
    }

    @NotNull
    @Override
    public Map<ID, Supplier<T>> getAll() {
        return this.options;
    }

    @NotNull
    @Override
    public IDynamic<T, ID> add(@NotNull final ID id, @NotNull final Supplier<T> objSupplier) {
        this.options.put(id, objSupplier);
        return this;
    }

    @NonNull
    @Override
    public IDynamic<T, ID> add(@NotNull final ID id, @NonNull final T obj) {
        return this.add(id, () -> obj);
    }
}
