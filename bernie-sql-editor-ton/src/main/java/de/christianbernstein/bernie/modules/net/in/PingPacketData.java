package de.christianbernstein.bernie.modules.net.in;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.Getter;

/**
 * @author Christian Bernstein
 */
@Getter
@PacketMeta(dataID = "PingPacketData", protocol = "core")
public class PingPacketData extends PacketData {

    private long outboundTimestamp;

}
