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

import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketServerLane;
import de.christianbernstein.bernie.shared.misc.DefaultDepletionManager;
import de.christianbernstein.bernie.shared.misc.IDepletionManager;
import lombok.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ResponseManager implements IPacketInterceptor {

    private final IDepletionManager<Response> responseManager = new DefaultDepletionManager<>(Executors.newSingleThreadScheduledExecutor());

    @Override
    public boolean intercept(@NonNull Packet<?> packet, @NonNull final PacketData data, @NonNull SocketServerLane session) {
        if (packet.getType() == PacketType.RESPONSE) {
            final Optional<Response> optional = this.responseManager.getSubjects().keySet().stream().filter(response -> response.id().equals(packet.getId())).findFirst();
            optional.ifPresent(response -> {
                response.callback().complete(data);
            });
            return false;
        } else {
            return true;
        }
    }

    public void registerResponse(@NonNull final String id, @NonNull final CompletableFuture<PacketData> future, final long amount, @NonNull final TimeUnit unit, @NonNull final Consumer<Response> timeoutHandler) {
        this.responseManager.register(new Response(id, future), timeoutHandler, amount, unit);
    }

    public void stop() {
        this.responseManager.stop();
    }
}
