package de.christianbernstein.bernie.modules.settings.out;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Getter
@PacketMeta(dataID = "SettingsResponsePacketData", protocol = "base")
@AllArgsConstructor
public class SettingsResponsePacketData extends PacketData {

    private Map<String, Object> payload;
}
