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

import de.christianbernstein.bernie.shared.misc.DefaultDepletionManager;
import de.christianbernstein.bernie.shared.misc.IDepletionManager;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
public class SocketSessionManager {

    private IDepletionManager<SocketServerLane> sessionStorage = new DefaultDepletionManager<>(
            Executors.newSingleThreadScheduledExecutor()
    );

    @SuppressWarnings("UnusedReturnValue")
    public SocketSessionManager addSession(@NonNull final SocketServerLane session, @NonNull final Consumer<SocketServerLane> onDepletion, final long amount, @NonNull final TimeUnit unit) {
        this.sessionStorage.register(session, onDepletion, amount, unit);
        return this;
    }

    @Nullable
    public SocketServerLane getSession(@NonNull final String id) {
        return this.sessionStorage.getSubjects().keySet().stream().filter(session -> session.getId().equals(id)).findFirst().orElse(null);
    }

    @NonNull
    public Set<SocketServerLane> getSessions() {
        return this.sessionStorage.getSubjects().keySet();
    }

    public void renew(SocketServerLane subject, long amount, TimeUnit unit) {
        this.sessionStorage.renew(subject, amount, unit);
    }

    public void renew(String laneID, long amount, TimeUnit unit) {
        this.sessionStorage.renew(getSession(laneID), amount, unit);
    }

    public void removeSession(@NonNull final SocketServerLane session) {
        this.sessionStorage.tryRemove(session);
    }

    public void removeSession(@NonNull final String id) {
        this.removeSession(Objects.requireNonNull(this.getSession(id)));
    }

    public void shutdown() {
        this.sessionStorage.stop();
    }

}
