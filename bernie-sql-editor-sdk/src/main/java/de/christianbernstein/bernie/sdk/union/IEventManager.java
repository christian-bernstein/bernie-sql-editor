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

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface IEventManager {

    <T extends Event> IEventManager registerFunctionEventListener(
            String channel, EventPriority priority, Class<? extends Event> event,
            IEventListenerFunction<T> function
    );

    IEventManager registerListener(Object listener);

    IEventManager registerListener(Class<?> listener);

    IEventManager unregisterAll();

    Map<String, List<IRegisteredEventListener>> getRegisteredListeners();

    IEventHook getEventHook();

    IEventManager setEventHook(IEventHook hook);

    <T extends Event> EventResult<T> fireEvent(String channel, T event);

    default <T extends Event> EventResult<T> fireEvent(T event) {
        return this.fireEvent("*", event);
    }

    default IEventManager registerListeners(Object... listeners) {
        Objects.requireNonNull(listeners);
        for (final Object o : listeners) {
            this.registerListener(o);
        }
        return this;
    }

}
