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

import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.discovery.ITransportServer;
import de.christianbernstein.bernie.shared.discovery.websocket.Protocol;
import de.christianbernstein.bernie.shared.discovery.websocket.ProtocolController;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketIdentifyingAttachment;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketShutdownReason;
import de.christianbernstein.bernie.shared.document.IDocument;
import de.christianbernstein.bernie.shared.event.EventAPI;
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.tailwind.IProteus;
import de.christianbernstein.bernie.shared.tailwind.Proteus;
import lombok.Getter;
import lombok.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * todo add other event fires to the methods
 *
 * @author Christian Bernstein
 */
public class StandaloneSocketServer extends WebSocketServer implements IFluently<StandaloneSocketServer>, EventAPI.IWithEventController<StandaloneSocketServer>, ITransportServer {

    private final Set<WebSocket> sockets = new HashSet<>();

    private final SocketSessionManager socketSessionManager = new SocketSessionManager();

    private final ServerConfiguration configuration;

    private final IProteus<ISocketServerPublicAPI> proteus = new Proteus<>(ISocketServerPublicAPI.class, new SocketServerPrivateAPI()).load();

    private final EventAPI.IEventController<StandaloneSocketServer> eventController = new EventAPI.DefaultEventController<>();

    private final Map<String, List<CountDownLatch>> syncLatches = new ConcurrentHashMap<>();

    public StandaloneSocketServer(@NonNull final ServerConfiguration configuration) {
        super(configuration.address());
        this.configuration = configuration;
        this.init();
    }



    public StandaloneSocketServer() {
        super(new InetSocketAddress(80));
        this.configuration = ServerConfiguration.defaultConfiguration;
        this.init();
    }

    // todo better method short-handing
    public void onPostEstablish(BiConsumer<SocketPostEstablishedEvent, IDocument<?>> handler) {
        this.getEventController().registerHandler(new EventAPI.Handler<>(SocketPostEstablishedEvent.class, handler));
    }

    // todo sync protocol change
    public void init() {
        // Set all the pre-registered standard protocol factories to the newly created socket instances
        if (this.configuration.registerDefaultStandardProtocols()) {
            this.onPostEstablish((socketPostEstablishedEvent, document) -> {
                final ProtocolController controller = socketPostEstablishedEvent.session().getProtocolController();
                this.configuration.standardProtocols().forEach(controller::register);
            });
        }
        // Enable base protocol set on socket post establish
        if (this.configuration.baseProtocol() != null) {
            // todo use shorthand func
            this.getEventController().registerHandler(new EventAPI.Handler<>(SocketPostEstablishedEvent.class, (event, document) -> {
                final Protocol protocol = event.getReference().configuration.baseProtocol();
                event.session().getProtocolController().registerBase(protocol);
            }));
        }
        // Enable default protocol set on socket post establish
        if (this.configuration.setDefaultProtocolOnPostEstablish() && this.configuration.defaultProtocol() != null) {
            // todo use shorthand func
            this.getEventController().registerHandler(new EventAPI.Handler<>(SocketPostEstablishedEvent.class, (event, document) -> {
                final Protocol protocol = event.getReference().configuration.defaultProtocol();
                event.session().getProtocolController().register(protocol.id(), controller -> protocol).changeProtocol(protocol.id());
            }));
        }
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "Server Lane", String.format("Opening ws server on port '%s'", this.getAddress().getPort()));
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, ClientHandshake clientHandshake) {
        this.getEventController().fire(new SocketPreEstablishedEvent(this, webSocket, clientHandshake));
        @NonNull final String id = this.configuration.websocketIDGenerator().apply(this, webSocket);
        final SocketIdentifyingAttachment attachment = new SocketIdentifyingAttachment(id);
        webSocket.setAttachment(attachment);
        this.sockets.add(webSocket);
        final SocketServerLane session = this.configuration.socketSessionGenerator().apply(webSocket);
        this.socketSessionManager.addSession(session, socketSession -> {
            // todo make better handling
            socketSession.shutdown(SocketShutdownReason.DEPLETION);
        }, this.configuration.sessionDepletionTimeAmount(), this.configuration.sessionDepletionTimeunit());
        this.proteus.external().onOpen(new OnOpenSocketContext(webSocket, session, this, clientHandshake));
        this.getEventController().fire(new SocketPostEstablishedEvent(this, session));
    }

    @Override
    public void onClose(@NotNull WebSocket webSocket, int i, String s, boolean b) {
        final SocketIdentifyingAttachment attachment = webSocket.getAttachment();
        final SocketServerLane session = this.socketSessionManager.getSession(attachment.sessionToken());
        if (session != null) {

            // todo create hook

            this.getEventController().fire(new SocketPreShutdownEvent(this, session));
            this.proteus.external().onStop(new OnStopSocketContext(webSocket, session, this));
            this.sockets.remove(webSocket);
            this.getSessionManager().removeSession(session);
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "Server Lane", String.format("Websocket closed '%s'", webSocket.<SocketIdentifyingAttachment>getAttachment().sessionToken()));
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, String message) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "Server Lane", String.format("Received message '%s'", message));
        final SocketIdentifyingAttachment attachment = webSocket.getAttachment();
        final SocketServerLane session = this.socketSessionManager.getSession(attachment.sessionToken());
        if (session != null) {
            final Duration duration = session.getConfiguration().sessionRenewalAmount();
            this.socketSessionManager.renew(session, duration.getSeconds(), TimeUnit.SECONDS);
            this.proteus.external().onMessage(new OnMessageSocketContext(webSocket, session, this, message));
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "Server Lane", String.format("SocketServerLane corresponding to token '%s' is null", attachment.sessionToken()));
        }
    }

    @Override
    public void onError(@NotNull WebSocket webSocket, Exception e) {
        final SocketIdentifyingAttachment attachment = webSocket.getAttachment();
        final SocketServerLane session = this.socketSessionManager.getSession(attachment.sessionToken());
        this.proteus.external().onError(new OnErrorSocketContext(webSocket, session, this, e));
    }

    @Override
    public void onStart() {
        this.proteus.external().onStart();
        this.fireSyncEvent(SyncLatchEvent.SERVER_OPEN);
    }

    public ITransportServer shutdown(@NonNull final SocketShutdownReason reason) throws InterruptedException {
        this.socketSessionManager.getSessions().forEach(lane -> {
            lane.shutdown(reason);
            this.socketSessionManager.removeSession(lane);
        });
        // todo check importance
        this.socketSessionManager.shutdown();
        try {
            this.stop((int) this.configuration.socketServerStopTimeout().toMillis());
        } catch (final InterruptedException e) {
            // e.printStackTrace();
            try {
                this.stop();
            } catch (final IOException e2) {
                // e2.printStackTrace();
            }
        }
        this.fireSyncEvent(SyncLatchEvent.SERVER_CLOSE);
        return this;
    }

    @NonNull
    public SocketSessionManager getSessionManager() {
        return this.socketSessionManager;
    }

    @Override
    public EventAPI.@NonNull IEventController<StandaloneSocketServer> getEventController() {
        return this.eventController;
    }

    @Override
    public @NonNull StandaloneSocketServer me() {
        return this;
    }

    @Nullable
    public WebSocket getWebsocketFromID(@NonNull final String id) {
        return this.sockets.stream().filter(socket -> socket.<SocketIdentifyingAttachment>getAttachment().sessionToken().equals(id)).findFirst().orElse(null);
    }

    private void fireSyncEvent(@NonNull final String latchSyncEvent) {
        if (this.syncLatches.containsKey(latchSyncEvent)) {
            this.syncLatches.get(latchSyncEvent).forEach(latch -> {
                try {
                    latch.countDown();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void fireSyncEvent(@NonNull final StandaloneSocketServer.SyncLatchEvent latchSyncEvent) {
        ConsoleLogger.def().log(
                ConsoleLogger.LogType.INFO,
                "Server Lane",
                String.format("Fire server latch sync event '%s'", latchSyncEvent)
        );
        this.fireSyncEvent(latchSyncEvent.getType());
    }

    public void sync(@NonNull final String latchSyncEvent, @IntRange(from = 1) final int latchSyncAmount) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(latchSyncAmount);
        if (!this.syncLatches.containsKey(latchSyncEvent)) {
            this.syncLatches.put(latchSyncEvent, List.of(latch));
        } else {
            this.syncLatches.get(latchSyncEvent).add(latch);
        }
        latch.await();
    }

    public void sync(final @NonNull StandaloneSocketServer.SyncLatchEvent latchSyncEvent, @IntRange(from = 1) final int latchSyncAmount) throws InterruptedException {
        this.sync(latchSyncEvent.getType(), latchSyncAmount);
    }

    public void syncOpen() throws InterruptedException {
        this.sync(SyncLatchEvent.SERVER_OPEN, 1);
    }

    public void syncClose() throws InterruptedException {
        this.sync(SyncLatchEvent.SERVER_CLOSE, 1);
    }

    @Getter
    public enum SyncLatchEvent {
        SERVER_CLOSE("server_close"), SERVER_OPEN("server_open");

        private final String type;

        SyncLatchEvent(String type) {
            this.type = type;
        }
    }
}
