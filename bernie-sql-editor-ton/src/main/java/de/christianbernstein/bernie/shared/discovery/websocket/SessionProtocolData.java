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

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
public class SessionProtocolData {

    @NonNull
    private final String protocolID;

    @Builder.Default
    private final boolean isBaseProtocol = false;

    @NonNull
    private final Map<String, Class<? extends PacketData>> packetRegistry = new HashMap<>();

    @NonNull
    private final Map<Class<? extends PacketData>, IPacketHandlerBase<?>> packetHandler = new HashMap<>();

    public <T extends PacketData> SessionProtocolData register(@NonNull String packetID, @NonNull Class<T> datatype, @NonNull IPacketHandlerBase<T> handler) {
        this.packetRegistry().put(packetID, datatype);
        this.packetHandler().put(datatype, handler);
        return this;
    }

    // todo load handlers from non-static classes
    // todo add protocol specific load
    // todo add only handler / packet registers
    public SessionProtocolData loadFromClass(@NonNull final Class<?> holder) {
        // Register all the static fields
        // System.out.println("Load protocol data: " + holder);
        Arrays.stream(holder.getDeclaredFields()).filter(field -> Modifier.isStatic(Modifier.fieldModifiers())).forEach(field -> {
            // System.out.println("Handle field: " + field);
            if (field.getType().equals(IPacketHandlerBase.class) && field.isAnnotationPresent(Discoverer.class)) {
                field.setAccessible(true);
                final Discoverer discoverer = field.getAnnotation(Discoverer.class);
                if (Arrays.asList(discoverer.protocols()).contains(this.protocolID)) {
                    try {
                        // System.out.println("Register packet for: " + discoverer);
                        final IPacketHandlerBase<?> handler = (IPacketHandlerBase<?>) field.get(null);
                        this.packetRegistry().put(discoverer.packetID(), discoverer.datatype());
                        this.packetHandler().put(discoverer.datatype(), handler);
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return this;
    }
}
