package de.christianbernstein.bernie.modules.db.in;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;

/**
 * @author Christian Bernstein
 */
@PacketMeta(dataID = "SessionLoadResourcesRequestPacketData", protocol = Constants.centralProtocolName)
public class SessionLoadResourcesRequestPacketData extends PacketData {
}
