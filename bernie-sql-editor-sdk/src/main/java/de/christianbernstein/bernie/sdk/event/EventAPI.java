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

package de.christianbernstein.bernie.sdk.event;

import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public final class EventAPI {

    // todo rename!
    public interface IWithEventController<T> {

        @NonNull
        IEventController<T> getEventController();

    }

    @RequiredArgsConstructor
    public abstract static class Event<T> {

        @Getter
        private final T reference;

    }

    public abstract static class CancelableEvent<T> extends Event<T> {

        private boolean canceled;

        public CancelableEvent(T reference) {
            super(reference);
        }

        public boolean isCanceled() {
            return this.canceled;
        }

        public void setCanceled(final boolean canceled) {
            this.canceled = canceled;
        }
    }

    public interface IEventController<T> {

        IEventController<T> registerHandler(Handler<?> handler);

        void holdEventController(boolean hold);

        <V extends Event<T>> V fire(V event);

        List<Handler<T>> getHandlersOf(Class<T> target);

        void removeIf(Predicate<Handler<T>> predicate);
    }

    @Data
    public static class Handler<T> {

        private final Class<T> target;

        private final IDocument<?> meta;

        private final BiConsumer<T, IDocument<?>> handler;

        public Handler(Class<T> target, BiConsumer<T, IDocument<?>> handler) {
            this.target = target;
            this.handler = handler;
            this.meta = Document.empty();
        }
    }

    public static class DefaultEventController<T> implements IEventController<T> {

        private final List<Handler<?>> eventHandlers = new ArrayList<>();

        private boolean onHold = false;

        @Override
        public IEventController<T> registerHandler(@NonNull Handler<?> handler) {
            this.eventHandlers.add(handler);
            return this;
        }

        @Override
        public void holdEventController(boolean hold) {
            this.onHold = hold;
        }

        @Override
        public <V extends Event<T>> V fire(V event) {
            if (this.onHold) {
                // todo handler better!
                return event;
            }
            this.eventHandlers.stream().filter(handler -> handler.getTarget().isAssignableFrom(event.getClass())).forEach(handler -> {
                try {
                    @SuppressWarnings("unchecked") final BiConsumer<V, IDocument<?>> consumer = (BiConsumer<V, IDocument<?>>) handler.getHandler();
                    consumer.accept(event, handler.getMeta());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return event;
        }

        @Override
        public List<Handler<T>> getHandlersOf(Class<T> target) {
            final List<Handler<?>> handlers = this.eventHandlers.stream().filter(handler -> handler.getTarget().equals(target)).collect(Collectors.toList());
            final List<Handler<T>> polishedHandlers = new ArrayList<>();
            handlers.forEach(handler -> {
                try {
                    @SuppressWarnings("unchecked") final Handler<T> polishedHandler = (Handler<T>) handler;
                    polishedHandlers.add(polishedHandler);
                } catch (final ClassCastException ignored) {
                }
            });
            return polishedHandlers;
        }

        // todo implement
        @Override
        public void removeIf(Predicate<Handler<T>> predicate) {
            new UnsupportedOperationException("not implemented yet!").printStackTrace();
        }
    }
}
