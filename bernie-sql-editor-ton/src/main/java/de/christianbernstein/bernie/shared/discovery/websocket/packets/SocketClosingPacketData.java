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

package de.christianbernstein.bernie.shared.discovery.websocket.packets;

import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketShutdownReason;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@PacketMeta(dataID = "SocketClosingPacketData", protocol = "core")
public class SocketClosingPacketData extends PacketData {

    /**
     * Prevents the client connector to reconnect to the server.
     * If this var is false, the client will potentially try
     * to restore the connection like after website reloading, backend restart,
     *
     * A typical example of this set to true is when the session
     * depleted. The connection will be closed in order to save resources
     * on the serverside. Therefore, the client shouldn't attempt a reconnection,
     * because that would prevent any resource saving.
     */
    private final boolean activateReconnectLock;

    private final SocketShutdownReason reason;

}
