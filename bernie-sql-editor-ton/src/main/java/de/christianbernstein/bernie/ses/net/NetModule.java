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
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketShutdownReason;
import de.christianbernstein.bernie.shared.discovery.websocket.packets.SocketSwitchProtocolDataPacket;
import de.christianbernstein.bernie.shared.discovery.websocket.server.ServerConfiguration;
import de.christianbernstein.bernie.shared.discovery.websocket.server.StandaloneSocketServer;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * @author Christian Bernstein
 */
public class NetModule implements INetModule {

    private final NetModuleConfig config = NetModuleConfig.builder()
            .serverConfiguration(ServerConfiguration.builder()
                    .setDefaultProtocolOnPostEstablish(true)
                    // todo support factory initiation
                    .defaultProtocol(Protocols.loginProtocol)
                    .standardProtocol(Constants.centralProtocolName, Protocols.centralProtocolFactory)
                    // todo refactor
                    .baseProtocol(Protocols.baseProtocolFactory)
                    .build())
            .syncClose(false)
            .build();

    private StandaloneSocketServer server;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        INetModule.super.boot(api, module, manager);
        this.server = new StandaloneSocketServer(this.config.getServerConfiguration());

        // Sync protocol changes to the client
        this.server.onPostEstablish((event, document) -> {
            event.session().getProtocolController().addProtocolChangeListener((from, to) -> {
                event.session().push(new SocketSwitchProtocolDataPacket(to));
            });
        });

        // Start the socket server
        this.server.start();
        if (this.config.isSyncOpen()) {
            try {
                this.server.syncOpen();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        INetModule.super.uninstall(api, module, manager);
        try {
            this.server.shutdown(SocketShutdownReason.CORE_SHUTDOWN);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        // todo remove sync close -> not implemented in the right manner here! -> sync in synced method flow, sync close after closing
        if (this.config.isSyncClose()) {
            try {
                this.server.syncClose();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.server = null;
            }
        } else {
            this.server = null;
        }
    }

    @Override
    public StandaloneSocketServer getSocketServer() {
        return this.server;
    }

    @Override
    public @NonNull INetModule me() {
        return this;
    }
}
