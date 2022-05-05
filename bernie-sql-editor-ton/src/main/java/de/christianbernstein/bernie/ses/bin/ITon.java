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

import de.christianbernstein.bernie.modules.auth.AuthModule;
import de.christianbernstein.bernie.modules.auth.IAuthModule;
import de.christianbernstein.bernie.modules.cdn.CDNModule;
import de.christianbernstein.bernie.modules.cdn.ICDNModule;
import de.christianbernstein.bernie.modules.config.ConfigModule;
import de.christianbernstein.bernie.modules.config.IConfigModule;
import de.christianbernstein.bernie.modules.db.DBModule;
import de.christianbernstein.bernie.modules.db.IDBModule;
import de.christianbernstein.bernie.modules.flow.FlowModule;
import de.christianbernstein.bernie.modules.flow.IFlowModule;
import de.christianbernstein.bernie.modules.net.INetModule;
import de.christianbernstein.bernie.modules.net.NetModule;
import de.christianbernstein.bernie.modules.profile.IProfileModule;
import de.christianbernstein.bernie.modules.profile.ProfileModule;
import de.christianbernstein.bernie.modules.project.IProjectModule;
import de.christianbernstein.bernie.modules.project.ProjectModule;
import de.christianbernstein.bernie.modules.session.ISessionModule;
import de.christianbernstein.bernie.modules.session.SessionModule;
import de.christianbernstein.bernie.modules.user.IUser;
import de.christianbernstein.bernie.modules.user.IUserModule;
import de.christianbernstein.bernie.modules.user.UserModule;
import de.christianbernstein.bernie.sdk.misc.IFluently;
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

     @NonNull
     default IDBModule dbModule() {
          return this.require(DBModule.class, "db_module");
     }

     @NonNull
     default ICDNModule cdnModule() {
          return this.require(CDNModule.class, "cdn_module");
     }

     @NonNull
     default IProfileModule profileModule() {
          return this.require(ProfileModule.class, "profile_module");
     }

     @NonNull
     default IConfigModule configModule() {
          return this.require(ConfigModule.class, ConfigModule.configModule.getName());
     }
}
