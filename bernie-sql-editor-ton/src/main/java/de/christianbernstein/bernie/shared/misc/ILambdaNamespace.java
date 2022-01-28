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

import java.util.Map;
import java.util.function.Consumer;

/**
 * todo implement lambda metrics
 * todo implement lambda performance monitoring
 * todo implement lambda accessors -> private, public
 * todo give information about the caller
 *
 * @author Christian Bernstein
 */
public interface ILambdaNamespace<T> {

    @NonNull
    Map<String, Lambda<T>> getLambdas();

    @NonNull
    String getNamespace();

    default boolean containsKey(@NonNull String name) {
        return this.getLambdas().containsKey(name);
    }

    @SuppressWarnings("UnusedReturnValue")
    default ILambdaNamespace<T> register(@NonNull String name, @NonNull Lambda<T> lambda) {
        this.getLambdas().putIfAbsent(name, lambda);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    default ILambdaNamespace<T> register(@NonNull String name, @NonNull Consumer<T> lambda) {
        this.getLambdas().putIfAbsent(name, Lambda.fromConsumer(lambda));
        return this;
    }

    default T run(@NonNull String lambda, @NonNull T context, Consumer<Throwable> exceptionHandler) {
        try {
            return this.getLambdas().get(lambda).apply(context);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return null;
        }
    }

    default T run(@NonNull String lambda, @NonNull T context) {
        return this.getLambdas().get(lambda).apply(context);
    }

}
