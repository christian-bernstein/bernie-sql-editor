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

import de.christianbernstein.bernie.modules.auth.AuthModule;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.modules.db.DBModule;
import de.christianbernstein.bernie.ses.discoverer.BaseDiscoverers;
import de.christianbernstein.bernie.ses.discoverer.ProjectModuleDiscoverers;
import de.christianbernstein.bernie.modules.flow.FlowModule;
import de.christianbernstein.bernie.modules.project.ProjectModule;
import de.christianbernstein.bernie.modules.session.SessionModule;
import de.christianbernstein.bernie.sdk.discovery.websocket.IProtocolFactory;
import de.christianbernstein.bernie.sdk.discovery.websocket.Protocol;
import de.christianbernstein.bernie.sdk.discovery.websocket.SessionProtocolData;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Protocols {

    public IProtocolFactory loginProtocolFactory = controller -> Protocol.builder()
            .id(Constants.loginProtocolName)
            .attachment(SessionProtocolData.builder()
                    .protocolID(Constants.loginProtocolName)
                    .isBaseProtocol(false)
                    .build()
                    .loadFromClass(SessionModule.class)
                    .loadFromClass(AuthModule.class))
            .build();

    @Deprecated
    public Protocol loginProtocol = Protocol.builder()
            .id(Constants.loginProtocolName)
            .attachment(SessionProtocolData.builder()
                    .protocolID(Constants.loginProtocolName)
                    .isBaseProtocol(false)
                    .build()
                    .loadFromClass(SessionModule.class)
                    .loadFromClass(AuthModule.class))
            .build();

    public IProtocolFactory centralProtocolFactory = controller -> Protocol.builder()
            .id(Constants.centralProtocolName)
            .attachment(SessionProtocolData.builder()
                    .protocolID(Constants.centralProtocolName)
                    .isBaseProtocol(false)
                    .build()
                    .loadFromClass(FlowModule.class)
                    .loadFromClass(ProjectModule.class)
                    .loadFromClass(ProjectModuleDiscoverers.class)
                    .loadFromClass(DBModule.class))
            .build();

    public Protocol baseProtocolFactory = Protocol.builder()
            .id("base")
            .attachment(SessionProtocolData.builder()
                    .protocolID("base")
                    .isBaseProtocol(true)
                    .build()
                    .loadFromClass(BaseDiscoverers.class))
            .build();


}
