package de.christianbernstein.bernie.modules.config.in;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.*;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "GetConfigRequestPacketData", protocol = Constants.centralProtocolName)
public class GetConfigRequestPacketData extends PacketData {

    private List<String> configNames;

}
