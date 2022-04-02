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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
@RequiredArgsConstructor
public
class DefaultDepletionManager<T> implements IDepletionManager<T> {

    @NonNull
    private final ScheduledExecutorService executorService;

    @Getter
    private final Map<T, ScheduledFuture<?>> subjects = new HashMap<>();

    @Getter
    private final Map<T, Consumer<T>> depletionConsumers = new HashMap<>();

    @Override
    public boolean register(T subject, Consumer<T> onDepletion, long amount, TimeUnit unit) {
        if (this.subjects.containsKey(subject)) {
            return false;
        }
        final ScheduledFuture<?> future = this.executorService.schedule(() -> {
            // System.out.println("[Depletion Manager] Session depleted, closing socket connection : " + subject);
            try {
                onDepletion.accept(subject);
                this.subjects.remove(subject);
                this.depletionConsumers.remove(subject);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, amount, unit);
        this.depletionConsumers.put(subject, onDepletion);
        this.subjects.put(subject, future);
        return true;
    }

    @Override
    public void stop() {
        try {
            this.executorService.shutdownNow();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.subjects.clear();
        this.depletionConsumers.clear();
    }

    @Override
    public boolean tryRemove(T subject) {
        if (subject == null) {
            return false;
        }
        final Optional<? extends ScheduledFuture<?>> future = this.subjects.entrySet().stream().filter(entry -> entry.getKey().equals(subject)).findFirst().map(Map.Entry::getValue);
        future.ifPresent(scheduledFuture -> {
            scheduledFuture.cancel(false);
            this.subjects.remove(subject);
            this.depletionConsumers.remove(subject);
        });
        return future.isPresent();
    }

    // todo does this even work?  - cancel - register
    @Override
    public void renew(T subject, long amount, TimeUnit unit) {
        // todo better logging
        // System.out.println("[Depletion Manager] Renewing session: " + subject);

        final Optional<Map.Entry<T, ScheduledFuture<?>>> entry = this.subjects.entrySet().stream().filter(tScheduledFutureEntry -> tScheduledFutureEntry.getKey().equals(subject)).findFirst();
        if (entry.isPresent()) {
            final Map.Entry<T, ScheduledFuture<?>> futureEntry = entry.get();
            final boolean couldCancel = futureEntry.getValue().cancel(false);
            if (couldCancel) {
                final Optional<Map.Entry<T, Consumer<T>>> depletionHandler = this.getDepletionConsumers().entrySet().stream().filter(tConsumerEntry -> tConsumerEntry.getKey().equals(subject)).findFirst();
                // todo better depletion getting -> no .get()
                this.register(futureEntry.getKey(), depletionHandler.get().getValue(), amount, unit);
            }
        }
    }
}
