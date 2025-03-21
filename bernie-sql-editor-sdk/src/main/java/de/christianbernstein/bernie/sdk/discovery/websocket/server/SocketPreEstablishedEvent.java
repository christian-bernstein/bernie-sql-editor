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

package de.christianbernstein.bernie.sdk.discovery.websocket.server;

import de.christianbernstein.bernie.sdk.event.EventAPI;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * @author Christian Bernstein
 */
@Getter
@Accessors(fluent = true)
public class SocketPreEstablishedEvent extends EventAPI.CancelableEvent<StandaloneSocketServer> {

    private final WebSocket socket;

    private final ClientHandshake clientHandshake;

    public SocketPreEstablishedEvent(@NonNull final StandaloneSocketServer reference, @NonNull final WebSocket socket, @NonNull final ClientHandshake clientHandshake) {
        super(reference);
        this.socket = socket;
        this.clientHandshake = clientHandshake;
    }
}
