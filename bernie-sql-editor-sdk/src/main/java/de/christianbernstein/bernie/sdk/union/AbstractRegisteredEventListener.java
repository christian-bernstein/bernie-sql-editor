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

package de.christianbernstein.bernie.sdk.union;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
@AllArgsConstructor()
public abstract class AbstractRegisteredEventListener implements IRegisteredEventListener {

    @Getter
    private final EventListener eventListener;

    @Getter
    private final EventPriority priority;

    @Getter
    private final Class<? extends Event> eventClass;


    public static class RegisteredMethodEventListener extends AbstractRegisteredEventListener {

        @Getter
        private final Object instance;

        @Getter
        private final Method handlerMethod;

        public RegisteredMethodEventListener(EventListener eventListener, EventPriority priority, Class<? extends Event> eventClass, Object instance, Method handlerMethod) {
            super(eventListener, priority, eventClass);
            this.instance = instance;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public <T extends Event> T fireEvent(@NonNull T event) throws Exception {
            Objects.requireNonNull(event);
            if (this.getEventClass().isAssignableFrom(event.getClass())) {
                try {
                    this.getHandlerMethod().setAccessible(true);
                    this.getHandlerMethod().invoke(this.getInstance(), event);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    throw new EventListenerException("An error in handler method " + getHandlerMethod().getName() + " in class " + getInstance().getClass(), e);
                }
            }
            return event;
        }
    }

    public static class RegisteredFunctionEventListener extends AbstractRegisteredEventListener {

        @Getter
        private final Function<Event, Event> handler;

        public RegisteredFunctionEventListener(EventListener eventListener, EventPriority priority, Class<? extends Event> eventClass, Function<Event, Event> handler) {
            super(eventListener, priority, eventClass);
            this.handler = handler;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Event> T fireEvent(T event) {
            Objects.requireNonNull(event);
            if (this.getEventClass().isAssignableFrom(event.getClass())) {
                return (T) this.getHandler().apply(event);
            }
            return null;
        }
    }
}
