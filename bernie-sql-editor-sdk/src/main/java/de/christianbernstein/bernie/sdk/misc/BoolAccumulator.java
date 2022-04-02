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

import lombok.Builder;
import lombok.Singular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@Builder
public class BoolAccumulator {

    @Singular
    private final List<Supplier<Boolean>> conditions;

    @Builder.Default
    private final SwitchType switchType = SwitchType.ONE_WAY_TO_FALSE;

    @SafeVarargs
    public final boolean get(Supplier<Boolean>... localConditions) {
        List<Supplier<Boolean>> listedConditions = new ArrayList<>(this.conditions);
        listedConditions.addAll(Arrays.asList(localConditions));
        final AtomicBoolean ref = new AtomicBoolean(this.switchType == SwitchType.ONE_WAY_TO_FALSE);
        listedConditions.forEach(booleanSupplier -> {
            if (booleanSupplier != null) {
                final Boolean aBoolean = booleanSupplier.get();
                if (this.switchType == SwitchType.ONE_WAY_TO_FALSE) {
                    if (!aBoolean) ref.set(false);
                } else if (this.switchType == SwitchType.ONE_WAY_TO_TRUE) {
                    if (aBoolean) ref.set(true);
                }
            }
        });
        return ref.get();
    }

    public boolean get() {
        return this.get((Supplier<Boolean>) null);
    }

    @Override
    public Object clone() {
        Object clone;
        try {
            clone = super.clone();
        } catch (final CloneNotSupportedException e) {
            clone = BoolAccumulator.builder().switchType(this.switchType).conditions(this.conditions).build();
        }
        return clone;
    }

    public enum SwitchType {

        ONE_WAY_TO_FALSE, ONE_WAY_TO_TRUE

    }
}
