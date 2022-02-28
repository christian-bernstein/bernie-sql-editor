package de.christianbernstein.bernie.modules.project.out;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;

/**
 * @author Christian Bernstein
 */
@PacketMeta(dataID = "ProjectCreateResponsePacketData", protocol = Constants.centralProtocolName)
public class ProjectCreateResponsePacketData extends PacketData {
}
