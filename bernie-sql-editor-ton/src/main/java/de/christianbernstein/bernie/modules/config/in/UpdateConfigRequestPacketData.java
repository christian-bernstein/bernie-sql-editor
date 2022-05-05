package de.christianbernstein.bernie.modules.config.in;

import de.christianbernstein.bernie.modules.config.Changelist;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "UpdateConfigRequestPacketData", protocol = Constants.centralProtocolName)
public class UpdateConfigRequestPacketData extends PacketData {

    private List<Changelist> changes;

}
