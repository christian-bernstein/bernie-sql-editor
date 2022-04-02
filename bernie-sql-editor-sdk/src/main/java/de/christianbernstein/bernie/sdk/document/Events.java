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

package de.christianbernstein.bernie.sdk.document;

import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Events {

    @Getter
    public static class EntryAddedEvent<T extends IDocument<T>> extends Event<T> {

        private final String key;

        private final Object value;

        public EntryAddedEvent(T context, String key, Object value) {
            super(context);
            this.key = key;
            this.value = value;
        }
    }

    @Getter
    public static class EntryRemovedEvent<T extends IDocument<T>> extends Event<T> {

        private final String key;

        private final Object value;

        public EntryRemovedEvent(T context, String key, Object value) {
            super(context);
            this.key = key;
            this.value = value;
        }
    }
}
