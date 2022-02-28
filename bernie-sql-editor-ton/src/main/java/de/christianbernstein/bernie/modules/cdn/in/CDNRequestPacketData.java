package de.christianbernstein.bernie.modules.cdn.in;

import de.christianbernstein.bernie.modules.cdn.CDNRequestBranch;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "CDNRequestPacketData", protocol = Constants.coreProtocolName)
@AllArgsConstructor
public class CDNRequestPacketData extends PacketData {

    private List<CDNRequestBranch> branches;

}
