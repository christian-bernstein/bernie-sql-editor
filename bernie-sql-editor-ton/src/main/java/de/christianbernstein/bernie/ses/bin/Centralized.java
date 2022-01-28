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

package de.christianbernstein.bernie.ses.bin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class Centralized<T> implements Supplier<T> {

    @Override
    public T get() {
        return null;
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Centralized<T> constify(final T instance) {
        return new Centralized<>() {
            @Override
            public T get() {
                return instance;
            }
        };
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Centralized<T> dynamic(final Supplier<T> supplier) {
        return (Centralized<T>) supplier;
    }
}
