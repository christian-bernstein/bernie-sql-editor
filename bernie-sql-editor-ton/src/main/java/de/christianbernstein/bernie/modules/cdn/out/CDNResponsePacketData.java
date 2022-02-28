package de.christianbernstein.bernie.modules.cdn.out;

import de.christianbernstein.bernie.modules.cdn.CDNResponse;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "CDNResponsePacketData", protocol = Constants.coreProtocolName)
@AllArgsConstructor
public class CDNResponsePacketData extends PacketData {

    private CDNResponse response;

}
