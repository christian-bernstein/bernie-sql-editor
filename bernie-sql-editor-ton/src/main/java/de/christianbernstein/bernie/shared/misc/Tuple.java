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

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
public class Tuple {

    enum TupleRelation {
        n(-1, DynamicTuple::new),
        n1(1, objects -> new Singleton<>(objects[0])),
        n2(2, objects -> new Couple<>(objects[0], objects[1])),
        n3(3, objects -> new Triple<>(objects[0], objects[1], objects[2])),
        n4(4, objects -> new Quadruple<>(objects[0], objects[1], objects[2], objects[3]));

        public static final int maxIdentifiableCount = 4;

        @Getter
        private final int mapped;

        @Getter
        private final Function<Object[], Tuple> tupleTransformer;

        TupleRelation(final int mapped, @NonNull final Function<Object[], Tuple> tupleTransformer) {
            this.mapped = mapped;
            this.tupleTransformer = tupleTransformer;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class Singleton<T> extends Tuple {
        private T x1;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class Couple<T, V> extends Tuple {
        private T x1;
        private V x2;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class Triple<T, V, S> extends Tuple {
        private T x1;
        private V x2;
        private S x3;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class Quadruple<T, V, S, X> extends Tuple {
        private T x1;
        private V x2;
        private S x3;
        private X x4;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class DynamicTuple extends Tuple {

        private Object[] data;

    }

    public static Tuple transformArrayToTuple(@NotNull @NonNull Object... array) {
        final int length = array.length;
        if (length > TupleRelation.maxIdentifiableCount) {
            return TupleRelation.n.getTupleTransformer().apply(array);
        } else {
            // todo better orElse handling -> throw, since maxMapped is inconsistent
            return Objects.requireNonNull(Arrays.stream(TupleRelation.values())
                    .filter(tupleRelation -> tupleRelation.getMapped() == length)
                    .findFirst().orElse(null)).getTupleTransformer().apply(array);
        }
    }

    // todo improve
    @SuppressWarnings("unchecked")
    public static <T extends Tuple> T genericTransformArrayToTuple(@NotNull @NonNull Object... array) {
        return (T) Tuple.transformArrayToTuple(array);
    }
}
