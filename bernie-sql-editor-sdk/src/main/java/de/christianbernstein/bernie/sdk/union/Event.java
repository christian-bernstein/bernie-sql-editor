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

import lombok.Getter;
import lombok.Setter;

/**
 * The root class for all events, which you want to fire with the EventManager implementations.
 * It is also meant to make it possible for listeners to have access to every object of every subclass.
 *
 * @author Christian Bernstein
 */
public abstract class Event {

    public abstract static class CancelableEvent extends Event implements ICancelable {

        @Getter
        @Setter
        private boolean cancelled = false;

    }
}
