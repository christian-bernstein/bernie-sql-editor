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

package de.christianbernstein.bernie.shared.discovery.websocket.server;

import de.christianbernstein.bernie.shared.discovery.ITransportLaneController;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.misc.IFluently;
import lombok.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
public class SocketTransportLaneController implements IFluently<SocketTransportLaneController>, ITransportLaneController<PacketData, SocketServerLane> {

    private final ServerConfiguration configuration;

    private StandaloneSocketServer endpoint;

    public SocketTransportLaneController(@NonNull final ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public SocketTransportLaneController() {
        this.configuration = ServerConfiguration.defaultConfiguration;
    }

    @Override
    public @NonNull Set<SocketServerLane> getLanes() {
        return this.endpoint.getSessionManager().getSessions();
    }

    @Override
    public @NonNull Optional<SocketServerLane> getLane(@NonNull String laneID) {
        return this.getLanes().stream().filter(session -> session.getId().equals(laneID)).findFirst();
    }

    @Override
    public @NonNull Set<SocketServerLane> filterViaID(@NonNull String filterRegex) {
        final Pattern pattern = Pattern.compile(filterRegex);
        return this.getLanes().stream().filter(session -> pattern.matcher(session.getId()).matches()).collect(Collectors.toSet());
    }

    @Override
    public @NonNull <In extends PacketData> ITransportLaneController<PacketData, SocketServerLane> broadcast(@NonNull In message) {
        // todo implement
        return this;
    }

    @Override
    public @NonNull ITransportLaneController<PacketData, SocketServerLane> start() {
        if (this.endpoint == null) {
            this.endpoint = new StandaloneSocketServer(this.configuration);
            this.endpoint.start();
        }
        return this;
    }

    @Override
    public @NonNull ITransportLaneController<PacketData, SocketServerLane> stop() {
        if (this.endpoint != null) {
            try {
                this.endpoint.stop();
            } catch (final IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public @NonNull SocketTransportLaneController me() {
        return this;
    }
}
