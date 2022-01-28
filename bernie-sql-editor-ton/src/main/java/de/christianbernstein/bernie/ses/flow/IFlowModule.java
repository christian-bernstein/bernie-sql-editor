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

package de.christianbernstein.bernie.ses.flow;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.user.IUser;
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("UnusedReturnValue")
public interface IFlowModule extends IBaseModuleClass<ITon>, IFluently<IFlowModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> flowModule = Module.<ITon>builder()
            .name("flow_module")
            .build()
            .$(module -> module.getShardManager().install(FlowModule.class));

    IFlowModule registerModule(String name, IFlow flow);

    IFlowModule removeModule(String name);

    Map<String, Object> run(IUser user, String name, Map<String, Object> parameter);

    boolean hasFlow(String name);
}
