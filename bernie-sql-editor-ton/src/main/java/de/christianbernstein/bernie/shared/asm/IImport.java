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

package de.christianbernstein.bernie.shared.asm;

import org.springframework.lang.NonNull;

public interface IImport {

    /**
     * @return True, if the import should be static
     */
    boolean isStatic();

    /**
     * @return The import path (without optional static modifier)
     */
    @NonNull
    String contract();

    /**
     * @return Stringify the import. This returns a valid Java 8 import section
     */
    @Override
    String toString();
}
