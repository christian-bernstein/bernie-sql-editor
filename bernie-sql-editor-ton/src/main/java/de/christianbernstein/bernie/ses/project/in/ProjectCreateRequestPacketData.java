package de.christianbernstein.bernie.ses.project.in;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "ProjectCreateRequestPacketData", protocol = Constants.centralProtocolName)
public class ProjectCreateRequestPacketData extends PacketData {

    private String title;

    private String description;

    private boolean stator;

}
