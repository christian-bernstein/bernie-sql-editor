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

package de.christianbernstein.bernie.sdk.discovery.websocket.packets;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.UtilityClass;

/**
 *
 * todo add other primitive packets
 *
 * @author Christian Bernstein
 */
@UtilityClass
public class Primitives {

    @Data
    @PacketMeta(dataID = "StringPacketData")
    @EqualsAndHashCode(callSuper = true)
    public static final class StringPacketData extends PacketData {

        private final String string;

    }

    @Data
    @PacketMeta(dataID = "IntegerPacketData")
    @EqualsAndHashCode(callSuper = true)
    public static final class IntegerPacketData extends PacketData {

        private final int anInt;

    }

    @Data
    @PacketMeta(dataID = "BooleanPacketData")
    @EqualsAndHashCode(callSuper = true)
    public static final class BooleanPacketData extends PacketData {

        private final boolean aBoolean;

    }

    @Data
    @PacketMeta(dataID = "DoublePacketData")
    @EqualsAndHashCode(callSuper = true)
    public static final class DoublePacketData extends PacketData {

        private final double aDouble;

    }

    @Data
    @PacketMeta(dataID = "LongPacketData")
    @EqualsAndHashCode(callSuper = true)
    public static final class LongPacketData extends PacketData {

        private final double aLong;

    }
}
