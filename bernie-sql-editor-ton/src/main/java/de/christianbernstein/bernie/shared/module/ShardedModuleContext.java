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

package de.christianbernstein.bernie.shared.module;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author Christian Bernstein
 */ // todo improve feature -> make tests to see which features are required
@RequiredArgsConstructor(staticName = "of")
public class ShardedModuleContext<T, V> implements IModuleContext<T> {

    @NonNull
    private final String stateKey;

    @NonNull
    private final Function<V, IModuleContext<T>> contextResolver;

    @Override
    public void fire(T api, @NotNull Module<T> module, IEngine<T> manager) {
        module.getState().<V>ifPresentOr(this.stateKey, v -> {
            this.contextResolver.apply(v).fire(api, module, manager);
        }, doc -> {
            System.out.println("unable to resolve state " + this.stateKey);
        });
    }
}
