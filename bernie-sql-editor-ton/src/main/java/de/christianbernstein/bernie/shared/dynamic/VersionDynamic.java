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

package de.christianbernstein.bernie.shared.dynamic;

import de.christianbernstein.bernie.shared.misc.Version;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class VersionDynamic<T> extends Dynamic<T, Version> {

    public VersionDynamic(@NonNull final Version recommendedID) {
        super(recommendedID);
    }

    @Nullable
    public T getNewest() {
        final List<Version> versions = this.getAll().keySet().stream().sorted().collect(Collectors.toList());
        final Version newestVersion = versions.get(versions.size() - 1);
        return this.get(newestVersion);
    }

    @Nullable
    public T getOldest() {
        final List<Version> versions = this.getAll().keySet().stream().sorted().collect(Collectors.toList());
        final Version oldestVersion = versions.get(0);
        return this.get(oldestVersion);
    }
}
