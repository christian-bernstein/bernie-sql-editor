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

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Bernstein
 */
@Deprecated
public interface ICrudeLogIntake {

    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    default void debug(@NonNull String message) {
        this.getLogger().debug(message);
    }

    default void info(@NonNull String message) {
        this.getLogger().info(message);
    }

    default void warn(@NonNull String message) {
        this.getLogger().warn(message);
    }

    default void error(@NonNull String message) {
        this.getLogger().error(message);
    }
}
