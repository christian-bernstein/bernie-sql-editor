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

package de.christianbernstein.bernie.sdk.discovery;

import lombok.NonNull;
import org.intellij.lang.annotations.Language;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * @author Christian Bernstein
 */
public interface ITransportLaneController<Element, Lane extends ITransportLane<Element>> {

    @NonNull
    Set<Lane> getLanes();

    @NonNull
    Optional<Lane> getLane(@NonNull final String laneID);

    @NonNull
    Set<Lane> filterViaID(@NonNull @Language("RegExp") final String filterRegex);

    @Nullable
    default Lane getLaneOptimistically(@NonNull final String laneID) {
        return this.getLane(laneID).orElse(null);
    }

    @NonNull
    <In extends Element> ITransportLaneController<Element, Lane> broadcast(@NonNull final In message);

    @NonNull
    ITransportLaneController<Element, Lane> start();

    @NonNull
    ITransportLaneController<Element, Lane> stop();
}
