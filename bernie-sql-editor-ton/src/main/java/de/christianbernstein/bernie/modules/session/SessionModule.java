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

package de.christianbernstein.bernie.modules.session;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.modules.auth.Credentials;
import de.christianbernstein.bernie.modules.session.in.ValidateSessionsPacketData;
import de.christianbernstein.bernie.modules.session.out.ValidateSessionsResponsePacketData;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.sdk.module.IEngine;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Christian Bernstein
 */
public class SessionModule implements ISessionModule {

    @SuppressWarnings("all")
    private static Optional<SessionModule> sessionModuleOptional;

    @SuppressWarnings("all")
    private static Optional<ITon> tonOptional;

    private final List<Session> sessions = new ArrayList<>();

    @Discoverer(packetID = "ValidateSessionsPacketData", datatype = ValidateSessionsPacketData.class, protocols = Constants.loginProtocolName)
    private static final IPacketHandlerBase<ValidateSessionsPacketData> validatePacketHandler = (data, endpoint, socket, packet, server) -> {
        final Map<UUID, Boolean> validationMap = new HashMap<>();
        final ISessionModule module = tonOptional.orElseThrow().sessionModule();
        data.getSessions().forEach(uuid -> {
            if (module.getOrCreateSession(uuid) == null) {
                // No session present
                validationMap.putIfAbsent(uuid, false);
            } else {
                // Session present, valid
                validationMap.putIfAbsent(uuid, true);
            }
        });
        // Send the response data to the validation map back to the client
        endpoint.respond(new ValidateSessionsResponsePacketData(validationMap), packet.getId());
    };

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        ISessionModule.super.boot(api, module, manager);
        SessionModule.sessionModuleOptional = Optional.of(this);
        SessionModule.tonOptional = Optional.of(api);


    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        ISessionModule.super.uninstall(api, module, manager);
        SessionModule.sessionModuleOptional = Optional.empty();
        SessionModule.tonOptional = Optional.empty();
    }

    @Override
    public Session getOrCreateSession(@NonNull Credentials credentials) {
        return this.sessions.stream().filter(session -> session.getCredentials().equals(credentials)).findFirst().orElseGet(() -> {
            // Create a session, because no session was registered yet.
            final Session session = new Session(UUID.randomUUID(), credentials, new Date());
            this.sessions.add(session);
            return session;
        });
    }

    @Override
    public Session getOrCreateSession(UUID sessionID) {
        if (sessionID == null) {
            return null;
        }
        return this.sessions.stream().filter(session -> session.getSessionID().equals(sessionID)).findFirst().orElse(null);
    }

    @Override
    public @NonNull ISessionModule me() {
        return this;
    }
}
