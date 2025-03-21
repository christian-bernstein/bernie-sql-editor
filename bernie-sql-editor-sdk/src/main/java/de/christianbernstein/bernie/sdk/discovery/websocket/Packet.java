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

package de.christianbernstein.bernie.sdk.discovery.websocket;

import com.google.gson.annotations.Expose;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.SocketServerLane;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class Packet<T extends PacketData> {

    private String protocol;

    private Date timestamp;

    // The type of data
    private String packetID;

    // the id of the packet, can be used to handle return packets
    private String id;

    private PacketType type;

    @Expose(deserialize = false)
    private T data;

    public T getData() {
        System.err.println("data can't be deserialized, this method returns null. Use 'data'-variable instead.");
        return this.data;
    }

    public <In extends PacketData> void respond(@NonNull In message, @NonNull final SocketServerLane lane) {
        if (this.type != PacketType.REQUEST) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "packet", "tried to respond to non-request packet");
            return;
        }
        lane.push(message, this.id, PacketType.RESPONSE);
    }
}
