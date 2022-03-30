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

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.discovery.websocket.server.StandaloneSocketServer;
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.misc.Resource;
import de.christianbernstein.bernie.shared.module.Dependency;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import de.christianbernstein.bernie.shared.module.Module;

/**
 * @author Christian Bernstein
 */
public interface INetModule extends IBaseModuleClass<ITon>, IFluently<INetModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> netModule = Module.<ITon>builder()
            .name("net_module")
            .dependency(Dependency.builder().module("auth_module").build())
            .build()
            .$(module -> module.getShardManager().install(NetModule.class));

    StandaloneSocketServer getSocketServer();

    Resource<NetModuleConfigShard> configResource();

    boolean isUsingSSL();
}
