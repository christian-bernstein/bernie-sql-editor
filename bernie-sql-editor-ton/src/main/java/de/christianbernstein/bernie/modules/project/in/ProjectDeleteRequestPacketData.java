package de.christianbernstein.bernie.modules.project.in;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "ProjectDeleteRequestPacketData", protocol = Constants.centralProtocolName)
public class ProjectDeleteRequestPacketData extends PacketData {

    private String id;

}
