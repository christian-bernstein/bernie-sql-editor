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

package de.christianbernstein.bernie.ses.net;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.auth.AuthModule;
import de.christianbernstein.bernie.ses.db.DBModule;
import de.christianbernstein.bernie.ses.discoverer.BaseDiscoverers;
import de.christianbernstein.bernie.ses.flow.FlowModule;
import de.christianbernstein.bernie.ses.project.ProjectModule;
import de.christianbernstein.bernie.ses.session.SessionModule;
import de.christianbernstein.bernie.shared.discovery.websocket.IProtocolFactory;
import de.christianbernstein.bernie.shared.discovery.websocket.Protocol;
import de.christianbernstein.bernie.shared.discovery.websocket.SessionProtocolData;
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
                    .loadFromClass(DBModule.class))
            .build();

    public Protocol baseProtocolFactory = Protocol.builder()
            .id("base")
            .attachment(SessionProtocolData.builder()
                    .protocolID("base")
                    .isBaseProtocol(true)
                    .build()
                    .loadFromClass(BaseDiscoverers.class)
                    // todo register base protocol stuff
                    )
            .build();


}
