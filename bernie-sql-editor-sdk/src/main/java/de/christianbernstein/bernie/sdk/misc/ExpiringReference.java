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
import lombok.NonNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@Builder
public class ExpiringReference<T> {

    private final long amount;

    private final TimeUnit unit;

    private final Supplier<T> lazySupplier;

    private final boolean valueNullValid, mayInterruptIfRunning;

    private final ScheduledExecutorService executorService;

    @NonNull
    private final AtomicReference<T> value;

    private ScheduledFuture<?> future;

    public T get() {
        if (this.value.get() == null && !this.valueNullValid) {
            if (this.lazySupplier == null) {
                // todo throw exception
            } else {
                this.value.set(this.lazySupplier.get());
            }
        }
        return this.value.get();
    }

    public ExpiringReference<T> set(T newValue) {
        if (!valueNullValid && newValue == null) {
            // todo throw something!
            return this;
        }
        this.value.set(newValue);
        return this.renew();
    }

    public ExpiringReference<T> renew() {
        if (!this.future.isDone()) {
            this.future.cancel(this.mayInterruptIfRunning);
        }
        this.future = this.executorService.schedule(() -> {
            value.set(null);
        }, this.amount, this.unit);
        return this;
    }
}
