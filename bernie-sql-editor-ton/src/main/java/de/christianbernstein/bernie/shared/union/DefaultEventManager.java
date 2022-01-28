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

package de.christianbernstein.bernie.shared.union;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Christian Bernstein
 */
public class DefaultEventManager implements IEventManager {

    @Getter
    private final Map<String, List<IRegisteredEventListener>> registeredListeners = new HashMap<>();

    @Getter
    private IEventHook eventHook;

    @Override
    public <T extends Event> IEventManager registerFunctionEventListener(String channel, EventPriority priority, Class<? extends Event> event, IEventListenerFunction<T> function) {
        return null;
    }

    @Override
    public IEventManager registerListener(Object listener) {
        // Get all methods of the class and check if the methods are listeners
        for (final Method method : listener.getClass().getDeclaredMethods()) {
            if (!(method.isAnnotationPresent(EventListener.class)
                    && method.getParameterCount() == 1
                    && Event.class.isAssignableFrom(method.getParameters()[0].getType()))) {
                continue;
            }
            final EventListener listenerAnnotation = method.getAnnotation(EventListener.class);
            @SuppressWarnings("unchecked") final IRegisteredEventListener registered = new AbstractRegisteredEventListener.RegisteredMethodEventListener(
                    listenerAnnotation,
                    listenerAnnotation.priority(),
                    (Class<? extends Event>) method.getParameters()[0].getType(),
                    listener, method
            );
            // If the channel doesn't exist yet, create it
            if (!this.registeredListeners.containsKey(listenerAnnotation.channel())) {
                this.registeredListeners.put(listenerAnnotation.channel(), new CopyOnWriteArrayList<>());
            }
            // Add the listener to the channel
            this.registeredListeners.get(listenerAnnotation.channel()).add(registered);
        }
        return this;
    }

    @Override
    public IEventManager unregisterAll() {
        this.registeredListeners.clear();
        return this;
    }

    @Override
    public IEventManager setEventHook(IEventHook hook) {
        this.eventHook = hook;
        return this;
    }

    @Override
    public <T extends Event> EventResult<T> fireEvent(String channel, T event) {
        if (channel == null || event == null) {
            throw new NullPointerException("Tried to fire event, but parameters aren't valid (channel='" + channel + "', event='" + event + "'");
        }
        // Check via event hook
        if (this.eventHook != null){
            if (!this.eventHook.approve(event, this)) {
                return new EventResult<>(Result.ResultState.FAILED, "The event hook cancelled the event", event, null);
            }
        }
        // Fire this event on every channel
        if (channel.equals("*")) {
            final List<IRegisteredEventListener> listeners = new ArrayList<>();
            this.registeredListeners.forEach((s, l) -> listeners.addAll(l));
            return this.fireEvent0(listeners, event);
        }
        // Validate, that the specific channel is available
        if (!this.registeredListeners.containsKey(channel)) {
            return new EventResult<>(Result.ResultState.FAILED, "The channel '" + channel + "' isn't available", event, null);
        }
        return this.fireEvent0(this.registeredListeners.get(channel), event);
    }

    private <T extends Event> EventResult<T> fireEvent0(List<IRegisteredEventListener> listeners, T event) {
        final List<EventResult.EventNote> notes = new ArrayList<>();
        Collections.sort(listeners);
        for (final IRegisteredEventListener listener : listeners) {
            try {
                listener.fireEvent(event);
            } catch (final EventListenerException e) {
                notes.add(EventResult.EventNote.of(listener, "A critical internal exception occurred", Level.ERROR, e));
            } catch (final Exception e) {
                notes.add(EventResult.EventNote.of(listener, "Exception reached end of bus-pipeline", Level.WARNING, e));
            }
        }
        return new EventResult<>(Result.ResultState.SUCCESS, "", event, notes);
    }
}
