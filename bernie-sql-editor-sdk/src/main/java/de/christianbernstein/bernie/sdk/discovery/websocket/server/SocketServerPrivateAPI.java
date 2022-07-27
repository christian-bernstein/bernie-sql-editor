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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.christianbernstein.bernie.sdk.discovery.websocket.*;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import de.christianbernstein.bernie.sdk.misc.ISerialAdapter;
import de.christianbernstein.bernie.sdk.tailwind.Gate;
import de.christianbernstein.bernie.sdk.tailwind.GateDefinition;
import de.christianbernstein.bernie.sdk.tailwind.PrivateAPI;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class SocketServerPrivateAPI extends PrivateAPI {

    private final ISerialAdapter serialAdapter = ISerialAdapter.defaultGsonSerialAdapter;

    @GateDefinition(id = "on-start")
    private final Gate<Void, Void> onStartGate = Gate.<Void, Void>builder()
            .service(voidVoidGate -> (unused, parameters, gate) -> {
                return null;
            })
            .build();

    @GateDefinition(id = "on-open")
    private final Gate<OnOpenSocketContext, Void> onOpenGate = Gate.<OnOpenSocketContext, Void>builder()
            .service(voidVoidGate -> (context, parameters, gate) -> {

                // context.session().addBaseRoutine(SampleProtocol.debugRoutine, true);

                // context.session().recompileRoutines();
                ConsoleLogger.def().log(
                        ConsoleLogger.LogType.INFO,
                        "Socket Server Private-API",
                        String.format("Socket opened '%s' with id '%s'", context.socket().getRemoteSocketAddress(), context.session().getId())
                );
                return null;
            })
            .build();

    @GateDefinition(id = "on-stop")
    private final Gate<OnStopSocketContext, Void> onStopGate = Gate.<OnStopSocketContext, Void>builder()
            .service(voidVoidGate -> (context, parameters, gate) -> {
                return null;
            })
            .build();

    @GateDefinition(id = "on-error")
    private final Gate<OnErrorSocketContext, Void> onErrorGate = Gate.<OnErrorSocketContext, Void>builder()
            .service(voidVoidGate -> (context, parameters, gate) -> {
                context.error().printStackTrace();
                return null;
            })
            .build();

    @GateDefinition(id = "on-message")
    private final Gate<OnMessageSocketContext, Void> onMessageGate = Gate.<OnMessageSocketContext, Void>builder()
            .service(voidVoidGate -> (OnMessageSocketContext context, @NonNull IDocument<?> parameters, Gate<OnMessageSocketContext, Void> gate) -> {
                // Apply transformers
                String message = context.message();
                for (@NonNull final UnaryOperator<String> transformer : context.session().getTransformers()) {
                    message = transformer.apply(message);
                }
                // Deserialize the packet, get data id und raw data
                final JsonElement tree = this.serialAdapter.deserialize(message, JsonObject.class);
                final String packetDataID = tree.getAsJsonObject().get("packetID").getAsString();
                final String jsonPacketData = tree.getAsJsonObject().get("data").toString();
                // Construct the packet data from the raw json string and pattern class
                final JsonElement jsonData = tree.getAsJsonObject().get("data");

                String transformed = message;
                context.session().getProtocolController().getBaseProtocols().forEach(protocol -> {
                    this.handlePacket(protocol, context, packetDataID, jsonData, transformed);
                });

                final Protocol protocol = context.session().getProtocolController().getActiveProtocol();
                if (protocol != null) {
                    this.handlePacket(protocol, context, packetDataID, jsonData, transformed);
                } else {
                    ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "(Active-)Protocol is null");
                }
                return null;
            })
            .build();

    private void handlePacket(@NonNull final Protocol protocol, @NonNull final OnMessageSocketContext context, @NonNull final String packetDataID, @NonNull final JsonElement jsonData, @NonNull final String message) {
        final SessionProtocolData attachment = protocol.attachment();
        final Class<? extends PacketData> dataPatternClass = attachment.packetRegistry().get(packetDataID);
        PacketData data = null;
        Packet<?> packet = this.serialAdapter.deserialize(message, Packet.class);
        if (dataPatternClass != null) {
            data = this.serialAdapter.deserialize(jsonData.toString(), dataPatternClass);
            boolean proceed = true;
            for (@NonNull final IPacketInterceptor interceptor : context.session().getInterceptors()) {
                if (!interceptor.intercept(packet, data, context.session())) {
                    proceed = false;
                    break;
                }
            }
            if (proceed) {
                final IPacketHandlerBase<?> handler = attachment.packetHandler().get(data.getClass());
                if (handler != null) {
                    handler._handle(data, context.session(), context.socket(), packet, context.server());
                } else {
                    System.err.printf("Packet handler in [%s] is null, but shouldn't be. [%s]%n", protocol.id(), attachment.packetHandler());
                }
            }
        } else {
            if (!((SessionProtocolData) protocol.attachment()).isBaseProtocol()) {
                // ConsoleLogger.def().log(
                //         ConsoleLogger.LogType.ERROR,
                //         "Socket Server Private-API",
                //         String.format("DataPatternClass is null: '%s' from requested proto '%s' in actual proto '%s'", packetDataID, packet.getProtocol(), Objects.requireNonNull(context.session().getProtocolController().getActiveProtocol()).id())
                // );
            }
        }
    }
}
