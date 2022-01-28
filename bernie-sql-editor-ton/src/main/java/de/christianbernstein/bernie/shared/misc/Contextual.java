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

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * todo write jdoc
 *
 * @param <T> Identifier
 * @param <V> Value
 * @author Christian Bernstein
 */
@SuppressWarnings("all")
public abstract class Contextual<T, V> {

    protected V current = null;

    public static <T, V> Contextual<T, V> wrap(@NonNull Supplier<Map<T, V>> rawSupplier) {
        return new Contextual<>() {
            @Override
            public Map<T, V> getRaw() {
                try {
                    return rawSupplier.get();
                } catch (final Exception e) {
                    e.printStackTrace();
                    return Map.of();
                }
            }
        };
    }

    @Nullable
    public V get() {
        return this.current;
    }

    public void use(@NonNull T identifier, @NonNull Consumer<V> user) {
        this.current = this.identify(identifier);
        user.accept(this.get());
        this.current = null;
    }

    public void use(@NonNull T identifier, @NonNull Runnable runnable) {
        this.use(identifier, ignored -> runnable.run());
    }

    public abstract Map<T, V> getRaw();

    // todo mark as dangerous -> usage can compromise other use() calles
    public V identify(T identifier) {
        final Optional<Map.Entry<T, V>> entry = this.getRaw().entrySet().stream().filter(tvEntry -> tvEntry.getKey().equals(identifier)).findFirst();
        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public Sync<T, V> sync() {
        return Sync.wrap(this::getRaw);
    }

    @Getter
    public static abstract class Sync<T, V> extends Contextual<T, V> {

        private boolean claimed = false;

        public static <T, V> Sync<T, V> wrap(@NonNull Supplier<Map<T, V>> rawSupplier) {
            return new Sync<>() {
                @Override
                public Map<T, V> getRaw() {
                    try {
                        return rawSupplier.get();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        return Map.of();
                    }
                }
            };
        }

        public Sync<T, V> claim(@NonNull T identifier) {
            synchronized (this) {
                while (this.claimed) {
                    try {
                        this.wait(500);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.claimed = true;
                this.current = this.identify(identifier);
            }
            return this;
        }

        public Sync<T, V> release() {
            this.current = null;
            this.claimed = false;
            synchronized (this) {
                this.notifyAll();
            }
            return this;
        }
    }
}
