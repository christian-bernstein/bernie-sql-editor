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

package de.christianbernstein.bernie.ses.user;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;

import java.util.UUID;
import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
public interface IUserModule extends IBaseModuleClass<ITon>, IFluently<IUserModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> userModule = Module.<ITon>builder()
            .name("user_module")
            .build()
            .$(module -> module.getShardManager().install(UserModule.class));

    boolean hasAccount(final String id);

    UserCreationResult plainCreateAccount(final UserData data);

    UserData getUserDataOfUsername(final String username);

    UserData getUserDataOf(final String id);

    IUser getUserOfUsername(final String username);

    IUser getUser(final String id);

    void updateUserData(final String id, final Function<UserData, UserData> updater);

    void plainDeleteUser(final String uuid);

    IUser root();

}
