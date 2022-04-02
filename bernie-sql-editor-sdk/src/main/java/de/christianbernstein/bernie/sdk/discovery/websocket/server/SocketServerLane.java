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

import com.google.common.reflect.TypeToken;
import de.christianbernstein.bernie.sdk.discovery.ITransportLane;
import de.christianbernstein.bernie.sdk.discovery.ITransportServerLane;
import de.christianbernstein.bernie.sdk.discovery.websocket.*;
import de.christianbernstein.bernie.sdk.discovery.websocket.packets.SocketClosingPacketData;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import de.christianbernstein.bernie.sdk.misc.ISerialAdapter;
import lombok.Data;
import lombok.NonNull;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Old name: SocketSession
 *
 * @author Christian Bernstein
 */
@Data
@SuppressWarnings("UnusedReturnValue")
public class SocketServerLane implements ITransportServerLane<PacketData> {

    // todo add constructor parameter
    private final SessionConfiguration configuration = SessionConfiguration.defaultConfiguration;

    private final IDocument<Document> attachments = new Document();

    private final ResponseManager responseManager = new ResponseManager();

    private final List<UnaryOperator<String>> transformers = new ArrayList<>();

    private final ProtocolController protocolController = new ProtocolController();

    private final Map<Class<? extends PacketData>, PacketMeta> packetMetaCache = new HashMap<>();

    private final ISerialAdapter serialAdapter = ISerialAdapter.defaultGsonSerialAdapter;

    private final List<IPacketInterceptor> interceptors = new ArrayList<>(List.of(responseManager));

    private final WebSocket socket;

    private final String id;

    private String sessionState;

    @NonNull
    public SocketServerLane callSessionState(@NonNull final String state) {
        this.getProtocolController().changeProtocol(state);
        this.sessionState = state;
        return this;
    }

    @NonNull
    public SocketServerLane addTransformer(@NonNull final UnaryOperator<String> transformator) {
        this.transformers.add(transformator);
        return this;
    }

    @NonNull
    public SocketServerLane addInterceptor(@NonNull final IPacketInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    @NonNull
    public SocketServerLane shutdown(@NonNull final SocketShutdownReason reason) {
        boolean activateReconnectLock = switch (reason) {
            case DEPLETION, CLIENT_SHUTDOWN_REQUEST -> true;
            case CORE_SHUTDOWN -> false;
        };

        this.push(new SocketClosingPacketData(activateReconnectLock, reason));
        ConsoleLogger.def().log(
                ConsoleLogger.LogType.INFO,
                "Server Lane",
                String.format("Closing websocket connection '%s'", this.id)
        );
        // todo is this needed?
        switch (reason) {
            case DEPLETION, CLIENT_SHUTDOWN_REQUEST -> this.socket.close();
        }
        this.responseManager.stop();
        return this;
    }

    @Override
    public @NonNull String getLaneIdentifier() {
        return this.id;
    }

    /**
     * @see SocketServerLane#push(PacketData) Simpler, yet less configurable push method call
     *
     * @param data The packet's payload.
     * @param id Packet-ID, used to identify packets, if errors occurred or POLL-packets return
     * @param type PacketType determines the type of handling by the other side:
     *             <code>PUSH</code> means no return value,
     *             <code>POLL</code> requires a return-packet to be sent with response data,
     *             <code>POLL_RESPONSE</code> is the response packet-type to a packet with POLL type
     * @param <T> Generic packet-data type
     */
    public <T extends PacketData> void push(@NonNull final T data, @NonNull final String id, @NonNull final PacketType type) {
        final Class<? extends PacketData> dataClass = data.getClass();
        // Retrieve or load packet meta
        PacketMeta meta = this.packetMetaCache.get(dataClass);
        if (meta == null) {
            // Not yet in cache, resolve and save it
            if (dataClass.isAnnotationPresent(PacketMeta.class)) {
                meta = dataClass.getAnnotation(PacketMeta.class);
                this.packetMetaCache.put(dataClass, meta);
            } else {
                // No annotation meta is present
                throw new IllegalArgumentException(String.format("Packet-Data-Class %s isn't annotated with @PacketMeta\n", dataClass));
            }
        }
        // Serialize and send packet
        @SuppressWarnings("all")
        final Type packetType = new TypeToken<Packet<T>>() {}.getType();
        final String serialized = this.serialAdapter.serialize(new Packet<>(meta.protocol(), new Date(), meta.dataID(), id, type, data), packetType);
        try {
            this.socket.send(serialized);
        } catch (final WebsocketNotConnectedException e) {
            // todo fix that exception IMPORTANT! -> WILL CAUSE SERVER CRASH IF ONLINE FOR TO LONG
            //      -> Remove the lane from the cache after the socket got closed
            // e.printStackTrace();
        }
    }

    /**
     * @see SocketServerLane#push(PacketData, String, PacketType) underlaying push method
     */
    @Override
    public @NonNull <In extends PacketData> ITransportLane<PacketData> push(@NonNull In message) {
        this.push(message, UUID.randomUUID().toString(), PacketType.SINGLETON);
        return this;
    }

    @Override
    public @NonNull <In extends PacketData> ITransportLane<PacketData> respond(@NonNull In message, @NonNull String conversationID) {
        this.push(message, conversationID, PacketType.RESPONSE);
        return this;
    }

    @Override
    public @NotNull <In extends PacketData, Out extends PacketData> ITransportLane<PacketData> poll(@NonNull final In data, @NonNull CompletableFuture<Out> callback) {
        final String id = UUID.randomUUID().toString();
        @SuppressWarnings("unchecked")
        final CompletableFuture<PacketData> future = (CompletableFuture<PacketData>) callback;
        this.responseManager.registerResponse(id, future, this.configuration.responseTimeoutAmount(), this.configuration.responseTimeoutUnit(), response -> {
            // TODO: 25.08.2021 Handle response timeout
            System.out.println("Response timed out");
        });
        this.push(data, id, PacketType.REQUEST);
        return this;
    }

    @Override
    public @NonNull ITransportLane<PacketData> shutdown(@Nullable final String reason) {
        return this.shutdown(SocketShutdownReason.valueOf(reason));
    }

    public <In extends PacketData, Out extends PacketData> ITransportLane<PacketData> poll(@NonNull final In data, @NonNull Consumer<Out> callback) {
        final CompletableFuture<Out> future = new CompletableFuture<>();
        future.thenAccept(callback);
        return this.poll(data, future);
    }
}
