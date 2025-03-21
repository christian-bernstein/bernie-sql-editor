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

package de.christianbernstein.bernie.modules.net;

import de.christianbernstein.bernie.sdk.discovery.websocket.server.ServerConfiguration;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetModuleConfig {

    public static final NetModuleConfig defaultConfig = NetModuleConfig.builder().build();

    @Builder.Default
    private boolean syncOpen = true;

    @Builder.Default
    private boolean syncClose = true;

    @Builder.Default
    private ServerConfiguration serverConfiguration = ServerConfiguration.defaultConfiguration;

}
