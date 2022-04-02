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
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class EventResult<T extends Event> extends Result {

    public static final String EVENT_TYPE = "event";

    @Getter
    private final T event;

    @Getter
    private final List<EventNote> notes;

    public EventResult(ResultState state, String status, T event, List<EventNote> notes) {
        super(state, status);
        this.event = event;
        if (notes == null) {
            this.notes = new ArrayList<>();
        } else this.notes = notes;
        this.type = EventResult.EVENT_TYPE;
    }

    @AllArgsConstructor(staticName = "of")
    @ToString
    public static class EventNote {

        @Getter
        private final IRegisteredEventListener listener;

        @Getter
        private final String note;

        @Getter
        private final Level level;

        @Getter
        private final Exception exception;
    }
}
