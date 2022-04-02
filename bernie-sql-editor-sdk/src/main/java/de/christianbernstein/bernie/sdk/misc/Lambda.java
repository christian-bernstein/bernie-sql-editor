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

import de.christianbernstein.bernie.sdk.document.IDocument;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * todo implement better log intake
 *
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface Lambda<T> extends Unsafe, ICrudeLogIntake, UnaryOperator<T> {

    @NotNull
    @Contract(pure = true)
    static <T> Lambda<T> identity() {
        return T -> T;
    }

    @NotNull
    @Contract(pure = true)
    static Lambda<IDocument<?>> docIdentity() {
        return T -> T;
    }

    @NotNull
    @Contract(pure = true)
    static <T> Lambda<T> fromConsumer(@NonNull Consumer<T> consumer) {
        return input -> {
            consumer.accept(input);
            return input;
        };
    }

    @NotNull
    @Contract(pure = true)
    static <T> Lambda<T> fromUnaryOperator(@NonNull UnaryOperator<T> operator) {
        return operator::apply;
    }
}
