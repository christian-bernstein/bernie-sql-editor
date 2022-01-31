package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.ses.UseTon;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.net.in.RequestServerFootprintPacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class BaseDiscoverers {

    @UseTon
    private ITon ton;

    private final IPacketHandlerBase<RequestServerFootprintPacketData> footprintHandler = (data, endpoint, socket, packet, server) -> {

    };
}
