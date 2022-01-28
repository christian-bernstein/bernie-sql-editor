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

package de.christianbernstein.bernie.shared.discovery;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Bernstein
 */
public class TransportLayer {

    private final Map<String, ITransportLaneController<?, ?>> serverController = new ConcurrentHashMap<>();

    private final Map<String, ITransportLaneController<?, ?>> clients = new ConcurrentHashMap<>();

    @NonNull
    @SuppressWarnings("unchecked")
    public <Lane extends ITransportLane<?>> Optional<ITransportLaneController<?, Lane>> getLaneController(@NonNull final String controllerID) {
        try {
            return Optional.ofNullable((ITransportLaneController<?, Lane>) this.serverController.get(controllerID));
        } catch (final ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Nullable
    public <Lane extends ITransportLane<?>> ITransportLaneController<?, Lane> getLaneControllerOptimistically(@NonNull final String controllerID) {
        return this.<Lane>getLaneController(controllerID).orElse(null);
    }

    @NonNull
    public TransportLayer registerLaneController(@NonNull final String controllerID, @NonNull final ITransportLaneController<?, ?> controller, final boolean autoStart) {
        this.serverController.put(controllerID, controller);
        if (autoStart) {
            controller.start();
        }
        return this;
    }

    @NonNull
    public TransportLayer registerLaneController(@NonNull final String controllerID, @NonNull final ITransportLaneController<?, ?> controller) {
        return this.registerLaneController(controllerID, controller, true);
    }
}
