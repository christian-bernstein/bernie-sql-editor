package de.christianbernstein.bernie.modules.config.out;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.*;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "GetConfigResponsePacketData", protocol = Constants.centralProtocolName)
public class GetConfigResponsePacketData extends PacketData {

    private Map<String, Map<String, Object>> configs;

}
