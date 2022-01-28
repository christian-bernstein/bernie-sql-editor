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

import de.christianbernstein.bernie.shared.tailwind.IPublicAPI;
import de.christianbernstein.bernie.shared.tailwind.Proteus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PublicAPIToClassTransformerTask {

    private Proteus<? extends IPublicAPI<?>> proteus;

    private Class<? extends IPublicAPI<?>> patternClass;

    private String implementationClassName;

    private String classFileLocation;

    private String packageName;

}
