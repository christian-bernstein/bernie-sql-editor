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

package de.christianbernstein.bernie.shared.tailwind;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * todo -> Implement
 *
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
public class ProteusConfig {

    @Builder.Default
    private final boolean autoLoadOnConstruct = true;

    @Builder.Default
    private final boolean fastboot = true;

    @Builder.Default
    private final String publicAPIClassNamingPattern = "%sImpl";
}
