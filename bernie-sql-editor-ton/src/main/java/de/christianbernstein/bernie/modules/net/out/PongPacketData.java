package de.christianbernstein.bernie.modules.net.out;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Christian Bernstein
 */
@NoArgsConstructor
@AllArgsConstructor
@PacketMeta(dataID = "PongPacketData", protocol = "core")
public class PongPacketData extends PacketData {

    private long inboundTimestamp;

    private long outboundTimestamp;
}
