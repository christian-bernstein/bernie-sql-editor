package de.christianbernstein.bernie.modules.project.out;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;

/**
 * @author Christian Bernstein
 */
@PacketMeta(dataID = "ProjectCreateResponsePacketData", protocol = Constants.centralProtocolName)
public class ProjectCreateResponsePacketData extends PacketData {
}
