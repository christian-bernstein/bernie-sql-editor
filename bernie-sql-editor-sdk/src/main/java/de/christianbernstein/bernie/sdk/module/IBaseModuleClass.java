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

package de.christianbernstein.bernie.sdk.module;

import org.jetbrains.annotations.NotNull;

/**
 * todo refactor!
 *
 * @author Christian Bernstein
 */
public interface IBaseModuleClass<T> {

    default void install(T api, @NotNull Module<T> module, IEngine<T> manager) {
    }

    default void boot(T api, @NotNull Module<T> module, IEngine<T> manager) {
    }

    default void uninstall(T api, @NotNull Module<T> module, IEngine<T> manager) {
    }
}
