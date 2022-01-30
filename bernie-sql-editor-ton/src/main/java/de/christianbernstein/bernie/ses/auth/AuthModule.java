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

package de.christianbernstein.bernie.ses.auth;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.auth.in.CredentialsLoginPacketData;
import de.christianbernstein.bernie.ses.auth.in.SessionIDLoginPacketData;
import de.christianbernstein.bernie.ses.auth.out.CredentialsLoginResponsePacketData;
import de.christianbernstein.bernie.ses.auth.out.SessionIDLoginResponsePacketData;
import de.christianbernstein.bernie.ses.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.ses.session.Session;
import de.christianbernstein.bernie.ses.user.IUser;
import de.christianbernstein.bernie.ses.user.UserData;
import de.christianbernstein.bernie.ses.user.UserProfileData;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketServerLane;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.IEngine;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public class AuthModule implements IAuthModule {

    @SuppressWarnings("all")
    private static Optional<AuthModule> instance = Optional.empty();

    @SuppressWarnings("all")
    private static Optional<ITon> ton = Optional.empty();

    @Discoverer(packetID = "CredentialsLoginPacketData", datatype = CredentialsLoginPacketData.class, protocols = "login")
    private static final IPacketHandlerBase<CredentialsLoginPacketData> loginByCredentialsHandler = (data, endpoint, socket, packet, server) -> {
        AtomicReference<CredentialsCheckResultType> result = new AtomicReference<>();
        // Authenticate via credentials in the auth module directly, if the instance bridge (AuthModule.instance) is
        // empty, consider that as an error.
        // This means, that the packet handler was called before the auth module was engaged in the module engine.
        AuthModule.instance.ifPresentOrElse(module -> {
            try {
                result.set(module.authByCredentials(data.getCredentials()));
            } catch (final Exception e) {
                e.printStackTrace();
                result.set(CredentialsCheckResultType.INTERNAL_ERROR);
            }
        }, () -> {
            System.err.println("Auth-module instance is empty");
            result.set(CredentialsCheckResultType.INTERNAL_ERROR);
        });
        // If the credentials were correct, get the session for the user via the given credentials
        // If no session is currently active, .getSession(...) will create a session automatically for
        // the given user.
        String sessionID = null;
        UserProfileData profileData = null;
        if (result.get() == CredentialsCheckResultType.OK) {
            final Session session = ton.orElseThrow().sessionModule().getOrCreateSession(data.getCredentials());
            sessionID = session.getSessionID().toString();
            profileData = AuthModule.ton.orElseThrow().userModule().getUser(session.getCredentials().getUsername()).getProfileData();
            System.err.println(profileData);

            AuthModule.setSocketLaneIdentifier(endpoint, session.getSessionID());
        }

        endpoint.respond(new CredentialsLoginResponsePacketData(result.get(), sessionID, profileData), packet.getId());

        if (result.get() == CredentialsCheckResultType.OK) {
            AuthModule.switchToCentralProtocol(endpoint);
        }
    };

    @Discoverer(packetID = "SessionIDLoginPacketData", datatype = SessionIDLoginPacketData.class, protocols = "login")
    private static final IPacketHandlerBase<SessionIDLoginPacketData> loginBySessionIDHandler = (data, endpoint, socket, packet, server) -> {
        System.err.println(AuthModule.instance.get());
        final UUID sessionID = data.getSessionID();
        AuthModule.instance.map(module -> module.authBySessionID(sessionID)).ifPresentOrElse(checkResultType -> {
            switch (checkResultType) {
                case OK -> {
                    // Get session from session module
                    final Session session = AuthModule.ton.orElseThrow().sessionModule().getOrCreateSession(sessionID);
                    // Retrieve username from session
                    final String username = session.getCredentials().getUsername();
                    // Retrieve user from username (maybe change to get user by id, not username)
                    final IUser user = AuthModule.ton.orElseThrow().userModule().getUser(username);

                    AuthModule.setSocketLaneIdentifier(endpoint, sessionID);

                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Respond login success packet to client
                    endpoint.respond(new SessionIDLoginResponsePacketData(checkResultType, user.getProfileData()), packet.getId());

                    // After successfully logging in via session id, change the socket protocol to the 'main' protocol
                    AuthModule.switchToCentralProtocol(endpoint);
                }
                case NO_SESSION_PRESENT -> {
                    // Respond login failed packet to client
                    endpoint.respond(new SessionIDLoginResponsePacketData(checkResultType, null), packet.getId());
                }
            }
        }, () -> {
            System.err.println("err");
        });
    };

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IAuthModule.super.boot(api, module, manager);
        AuthModule.instance = Optional.of(this);
        AuthModule.ton = Optional.of(api);
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IAuthModule.super.uninstall(api, module, manager);
        AuthModule.instance = Optional.empty();
        AuthModule.ton = Optional.empty();
    }

    @Override
    public CredentialsCheckResultType authByCredentials(@NonNull Credentials credentials) {
        UserData userData;
        // Get the user via the username from the credentials in a fail-proofed way
        try {
            userData = AuthModule.ton.orElseThrow().userModule().getUserDataOf(credentials.getUsername());
        } catch (final Exception e) {
            e.printStackTrace();
            return CredentialsCheckResultType.INTERNAL_ERROR;
        }

        // If the data is null, that means no user was found matching usernames
        if (userData == null) {
            return CredentialsCheckResultType.UNKNOWN_USERNAME;
        } else {
            // Check the password's correctness in a fail-proofed way
            try {
                return userData.getPassword().equals(credentials.getPassword()) ? CredentialsCheckResultType.OK : CredentialsCheckResultType.INCORRECT_PASSWORD;
            } catch (final Exception e) {
                e.printStackTrace();
                return CredentialsCheckResultType.INTERNAL_ERROR;
            }
        }
    }

    @Override
    public SessionIDCheckResultType authBySessionID(@NonNull UUID sessionID) {
        final Session session = AuthModule.ton.orElseThrow().sessionModule().getOrCreateSession(sessionID);
        if (session != null) {
            return SessionIDCheckResultType.OK;
        } else {
            return SessionIDCheckResultType.NO_SESSION_PRESENT;
        }
    }

    @Override
    public @NonNull IAuthModule me() {
        return this;
    }

    private static void setSocketLaneIdentifier(@NonNull SocketServerLane lane, @NonNull UUID sessionID) {
        System.err.println("Auth module: Setting SLI: " + sessionID);
        lane.getAttachments().putObjectIfAbsent(SocketLaneIdentifyingAttachment.ATTACHMENT_NAME, new SocketLaneIdentifyingAttachment(sessionID));
    }

    private static void switchToCentralProtocol(@NonNull SocketServerLane lane) {
        lane.getProtocolController().changeProtocol(Constants.centralProtocolName);
        // todo that is already done by the protocol change listener
        // lane.push(new SocketSwitchProtocolDataPacket(Constants.centralProtocolName));
    }
}
