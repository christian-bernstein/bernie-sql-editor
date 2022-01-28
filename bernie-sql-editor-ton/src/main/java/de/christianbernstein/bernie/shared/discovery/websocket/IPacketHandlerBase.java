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

package de.christianbernstein.bernie.shared.discovery.websocket;

import de.christianbernstein.bernie.shared.discovery.ITransportServer;
import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketServerLane;
import lombok.NonNull;
import org.java_websocket.WebSocket;

public interface IPacketHandlerBase<T extends PacketData> {

    @SuppressWarnings("unchecked")
    default void _handle(@NonNull final Object data, @NonNull final SocketServerLane session, @NonNull final WebSocket socket, @NonNull final Packet<?> packet, @NonNull final ITransportServer server) {
        try {
            this.handle((@NonNull T) data, session, socket, (Packet<T>) packet, server);
        } catch (final ClassCastException e) {
            e.printStackTrace();
        }
    }

    void handle(@NonNull final T data, @NonNull final SocketServerLane endpoint, @NonNull final WebSocket socket, @NonNull final Packet<T> packet, @NonNull final ITransportServer server);

}
