package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.ses.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.ses.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.ses.project.ProjectData;
import de.christianbernstein.bernie.ses.project.in.CheckProjectExistenceRequestPacketData;
import de.christianbernstein.bernie.ses.project.out.CheckProjectExistenceResponsePacketData;
import de.christianbernstein.bernie.ses.session.Session;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ProjectModuleDiscoverers {

    @UseTon
    private ITon ton;

    @Discoverer(packetID = "CheckProjectExistenceRequestPacketData", datatype = CheckProjectExistenceRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<CheckProjectExistenceRequestPacketData> checkProjectExistenceHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final UUID ownerUUID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final List<ProjectData> projects = ton.projectModule().getProjectsFromOwner(ownerUUID);
        final boolean match = projects.stream().anyMatch(pd -> pd.getTitle().equalsIgnoreCase(data.getTitle()));
        endpoint.respond(new CheckProjectExistenceResponsePacketData(match), packet.getId());
    };
}
