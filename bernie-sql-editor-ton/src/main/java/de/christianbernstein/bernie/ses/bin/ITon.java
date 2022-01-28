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
package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.auth.AuthModule;
import de.christianbernstein.bernie.ses.auth.IAuthModule;
import de.christianbernstein.bernie.ses.flow.FlowModule;
import de.christianbernstein.bernie.ses.flow.IFlowModule;
import de.christianbernstein.bernie.ses.net.INetModule;
import de.christianbernstein.bernie.ses.net.NetModule;
import de.christianbernstein.bernie.ses.project.IProjectModule;
import de.christianbernstein.bernie.ses.project.ProjectModule;
import de.christianbernstein.bernie.ses.session.ISessionModule;
import de.christianbernstein.bernie.ses.session.SessionModule;
import de.christianbernstein.bernie.ses.user.IUser;
import de.christianbernstein.bernie.ses.user.IUserModule;
import de.christianbernstein.bernie.ses.user.UserModule;
import de.christianbernstein.bernie.shared.misc.IFluently;
import lombok.NonNull;

import java.util.UUID;

// todo implement crash-recovery
@SuppressWarnings("UnusedReturnValue")
public interface ITon extends ITonBase<ITon>, IFluently<ITon> {

     IUser getUserFromSessionID(@NonNull UUID sessionID);

     @NonNull
     default ISessionModule sessionModule() {
          return this.require(SessionModule.class, "session_module");
     }

     @NonNull
     default IUserModule userModule() {
          return this.require(UserModule.class, "user_module");
     }

     @NonNull
     default INetModule netModule() {
          return this.require(NetModule.class, "net_module");
     }

     @NonNull
     default IAuthModule authModule() {
          return this.require(AuthModule.class, "auth_module");
     }

     @NonNull
     default IFlowModule flowModule() {
          return this.require(FlowModule.class, "flow_module");
     }

     @NonNull
     default IProjectModule projectModule() {
          return this.require(ProjectModule.class, "project_module");
     }
}

