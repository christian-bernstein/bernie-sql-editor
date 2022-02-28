/*
 * Copyright (C) 2022 Christian Bernstein
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

package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.modules.session.Session;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketIdentifyingAttachment;
import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketServerLane;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.java_websocket.WebSocket;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public final class Shortcut {

    @UseTon
    private ITon ton;

    public SocketLaneIdentifyingAttachment useSLI(@NonNull SocketServerLane lane) {
        return lane.getAttachments().get(SocketLaneIdentifyingAttachment.ATTACHMENT_NAME);
    }

    public Session useUserSession(@NonNull SocketServerLane lane) {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(lane);
        return ton.sessionModule().getOrCreateSession(sli.getSessionID());
    }

    public String useLaneID(@NonNull SocketServerLane lane) {
        return lane.getId();
    }

    public SocketIdentifyingAttachment useSI(@NonNull WebSocket socket) {
        return socket.getAttachment();
    }
}
