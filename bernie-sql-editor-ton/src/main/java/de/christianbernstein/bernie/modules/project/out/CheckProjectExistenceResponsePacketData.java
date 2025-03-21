package de.christianbernstein.bernie.modules.project.out;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "CheckProjectExistenceResponsePacketData", protocol = Constants.centralProtocolName)
public class CheckProjectExistenceResponsePacketData extends PacketData {

    private boolean doesExist;

}
