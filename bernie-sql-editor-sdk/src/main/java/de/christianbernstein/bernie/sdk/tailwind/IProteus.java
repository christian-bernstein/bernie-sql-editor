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
 * IProteus is a part of the Tailwind API-toolkit builder.
 * It lets you create a public- and private-api. The public-api is meant to be used to work with the api, todo syntax lol
 * the private-api does all the heavy logic lifting. Proteus is the middleman between the public- and private-api,
 * while the public-api is easy accessible to the developer, it contains no logic.
 * The private-api therefore has all the logic parts, but isn't viable to be used as a public. todo finish up rofl
 *
 * ""
 * protean, meaning "versatile", "mutable", or "capable of assuming many forms". "Protean" has positive connotations of flexibility, versatility and adaptability.
 * "" - Wikipedia about Proteus in Greek Mythology
 *
 * @param <Public> Generic public-api type.
 */
public interface IProteus<Public extends IPublicAPI> extends IFluently<IProteus<Public>> {

    @NonNull
    PrivateAPI internal();

    @NonNull
    Public external();

    @NonNull
    IProteus<Public> load();

    @NonNull
    default IEnhancedProteus<Public> enhance() {
        return (IEnhancedProteus<Public>) this;
    }
}
