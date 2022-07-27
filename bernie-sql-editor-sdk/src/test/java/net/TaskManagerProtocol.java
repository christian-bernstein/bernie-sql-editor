package net;

import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class TaskManagerProtocol {

    @Discoverer(packetID = "SysVolumeChangePacketData", datatype = Main.SysVolumeChangePacketData.class, protocols = "main")
    private final IPacketHandlerBase<Main.SysVolumeChangePacketData> volChangeHandler = (data, endpoint, socket, packet, server) -> {
        System.err.println("SysVolumeChangePacketData");

        if (data.isToggleMute()) {
        } else {
            if (data.isMute()) {
                Main.cmd("svcl /Mute \"Kopfhörer\"");
            } else {
                Main.cmd("svcl /Unmute \"Kopfhörer\"");
            }
        }

        if (data.getSoundOffset() != 0) {
            Main.cmd(String.format("svcl /ChangeVolume \"Kopfhörer\" %s", data.getSoundOffset()));
        }
    };
}
