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

package de.christianbernstein.bernie.sdk.tailwind;

import de.christianbernstein.bernie.sdk.misc.IFluently;
import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
public class PublicAPI<T extends IPublicAPI<?>> implements IFluently<IPublicAPI<T>>, IPublicAPI<T> {

    private IProteus<T> proteus;

    public PublicAPI(@NonNull final IProteus<T> proteus) {
        this.proteus = proteus;
    }

    /**
     *
     * @param proteus New proteus instance
     * @return Fluent this value
     */
    @NonNull
    public IPublicAPI<T> proteus(@NonNull IProteus<T> proteus) {
        this.proteus = proteus;
        return this;
    }

    @NonNull
    public IProteus<T> proteus() {
        return proteus;
    }

    @Override
    public @NonNull IPublicAPI<T> me() {
        return this;
    }
}
