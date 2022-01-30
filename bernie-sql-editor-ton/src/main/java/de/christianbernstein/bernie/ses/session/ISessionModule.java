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

package de.christianbernstein.bernie.ses.session;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.auth.Credentials;
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;

import java.util.UUID;

/**
 * @author Christian Bernstein
 */
public interface ISessionModule extends IBaseModuleClass<ITon>, IFluently<ISessionModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> sessionModule = Module.<ITon>builder()
            .name("session_module")
            .build()
            .$(module -> module.getShardManager().install(SessionModule.class));

    Session getOrCreateSession(@NonNull Credentials credentials);

    Session getOrCreateSession(UUID sessionID);

}
