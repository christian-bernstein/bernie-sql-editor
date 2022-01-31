package de.christianbernstein.bernie.ses.net.out;

import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PacketMeta(dataID = "ResponseServerFootprintPacketData", protocol = "core")
public class ResponseServerFootprintPacketData extends PacketData {

    private String currentProtocolName;

    private String serverTitle;

    private String serverDescription;

    private boolean debug;

}
