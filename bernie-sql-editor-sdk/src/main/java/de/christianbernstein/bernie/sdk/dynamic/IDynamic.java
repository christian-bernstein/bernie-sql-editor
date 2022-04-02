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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is meant to be used to for versioning systems.
 * If you have access to a variable from different places, it can become tedious to change the access, if you integrate
 * a new instance / implementation.
 * Typically, you have to go through each 'de.christianbernstein.bernie.ses.ton.user' of the old variable / instance and change the call of the old one to
 * the newly implemented one.
 * When you use {@link IDynamic} as a proxy for your implementations, you can implement a new version without changing
 * anything at the calling-layer of that variable / instance.
 *
 * For totally dynamic usage at the calling-layer use:
 * {@link IDynamic#getRecommended()}
 *
 * Default provided base-implementation
 * @see Dynamic
 *
 * @param <T> Map value type
 * @param <ID> Map key type
 */
public interface IDynamic<T, ID> {

    @Nullable
    T getRecommended();

    @Nullable
    T get(@NonNull ID id);

    @NonNull
    IDynamic<T, ID> recommendedID(@NonNull final ID newRecommendedID);

    @NonNull
    ID recommendedID();

    @NonNull
    Map<ID, Supplier<T>> getAll();

    @NonNull
    IDynamic<T, ID> add(@NonNull final ID id, @NonNull final T obj);

    @NonNull
    IDynamic<T, ID> add(@NonNull final ID id, @NonNull final Supplier<T> objSupplier);
}
