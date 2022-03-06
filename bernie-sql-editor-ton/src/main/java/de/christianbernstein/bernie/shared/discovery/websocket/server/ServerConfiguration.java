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

import de.christianbernstein.bernie.shared.discovery.websocket.IProtocolFactory;
import de.christianbernstein.bernie.shared.discovery.websocket.ISocketSessionGenerator;
import de.christianbernstein.bernie.shared.discovery.websocket.Protocol;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketIdentifyingAttachment;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Christian Bernstein
 */
@Getter
@Builder
@ToString
@Accessors(fluent = true)
public class ServerConfiguration {

    public static final ServerConfiguration defaultConfiguration = ServerConfiguration.builder()
            // todo remove dirty patch (1)
            .address(new InetSocketAddress(25574))
            .build();

    // todo remove dirty patch (1)
    @Builder.Default
    // private final InetSocketAddress address = new InetSocketAddress(80);
    private final InetSocketAddress address = new InetSocketAddress(25574);

    @Builder.Default
    private final ISocketIDGenerator websocketIDGenerator = (websocketEndpoint, webSocket) -> UUID.randomUUID().toString();

    @Builder.Default
    private final ISocketSessionGenerator socketSessionGenerator = socket -> new SocketServerLane(socket, socket.<SocketIdentifyingAttachment>getAttachment().sessionToken());

    @Builder.Default
    private final TimeUnit sessionDepletionTimeunit = TimeUnit.DAYS;

    @Builder.Default
    private final long sessionDepletionTimeAmount = Integer.MAX_VALUE;

    @Builder.Default
    private final Duration socketServerStopTimeout = Duration.of(10, ChronoUnit.SECONDS);

    @Builder.Default
    private final boolean setDefaultProtocolOnPostEstablish = false;

    /**
     * Standard protocols (no differentiation between base- and non-base-protocols!)
     *
     * @see ServerConfiguration#registerDefaultStandardProtocols controls the settting behaiviour
     */
    @Singular
    private final Map<String, IProtocolFactory> standardProtocols;

    @Builder.Default
    private final boolean registerDefaultStandardProtocols = true;

    private final Protocol defaultProtocol;

    private final Protocol baseProtocol;
}
