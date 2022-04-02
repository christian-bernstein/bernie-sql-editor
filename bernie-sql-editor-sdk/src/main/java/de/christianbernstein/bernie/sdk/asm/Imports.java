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

package de.christianbernstein.bernie.sdk.asm;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: 02.08.2021 Auto-Star-Import after certain amount of imports from the same package
public class Imports extends ArrayList<IImport> implements IImports {

    // TODO: 02.08.2021 Implement this method (Remove all the duplications, replace with star imports)
    @NotNull
    @Override
    public IImports compress() {
        // Remove each duplicate import
        final List<IImport> distinctImports = this.stream().distinct().collect(Collectors.toUnmodifiableList());
        this.clear();
        this.addAll(distinctImports);
        return this;
    }

    @Override
    public String toString() {
        return this.compress().stream().map(IImport::toString).collect(Collectors.joining("\n"));
    }
}
