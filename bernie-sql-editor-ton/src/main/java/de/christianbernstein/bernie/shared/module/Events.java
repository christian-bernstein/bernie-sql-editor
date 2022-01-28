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

package de.christianbernstein.bernie.shared.module;

import de.christianbernstein.bernie.shared.event.EventAPI;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public final class Events {

    @Getter
    public static final class ModuleAddedEvent<T> extends EventAPI.Event<IEngine<T>> {

        private final Module<T> module;

        public ModuleAddedEvent(IEngine<T> reference, Module<T> module) {
            super(reference);
            this.module = module;
        }
    }

    @Getter
    public static final class ModuleCalibrationChangeEvent<T> extends EventAPI.Event<IEngine<T>> {

        private final String from, to;

        public ModuleCalibrationChangeEvent(IEngine<T> reference, String from, String to) {
            super(reference);
            this.from = from;
            this.to = to;
        }
    }

    @Getter
    public static final class ModuleRemovedEvent<T> extends EventAPI.Event<IEngine<T>> {

        private final Module<T> module;

        public ModuleRemovedEvent(IEngine<T> reference, Module<T> module) {
            super(reference);
            this.module = module;
        }
    }
}
