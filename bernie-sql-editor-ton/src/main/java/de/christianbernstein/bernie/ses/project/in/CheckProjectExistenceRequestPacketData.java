package de.christianbernstein.bernie.ses.project.in;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "CheckProjectExistenceRequestPacketData", protocol = Constants.centralProtocolName)
public class CheckProjectExistenceRequestPacketData extends PacketData {

    private String title;

}
