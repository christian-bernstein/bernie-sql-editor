package de.christianbernstein.bernie.modules.settings.in;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Christian Bernstein
 */
@Getter
@PacketMeta(dataID = "SettingsRequestPacketData", protocol = "base")
@AllArgsConstructor
public class SettingsRequestPacketData extends PacketData {

    private String compoundID;

}
