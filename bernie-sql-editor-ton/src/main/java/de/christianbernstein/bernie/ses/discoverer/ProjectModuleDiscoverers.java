package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.ses.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.ses.project.IProjectModule;
import de.christianbernstein.bernie.ses.project.ProjectData;
import de.christianbernstein.bernie.ses.project.in.CheckProjectExistenceRequestPacketData;
import de.christianbernstein.bernie.ses.project.in.ListProjectPacketData;
import de.christianbernstein.bernie.ses.project.in.ProjectCreateRequestPacketData;
import de.christianbernstein.bernie.ses.project.out.CheckProjectExistenceResponsePacketData;
import de.christianbernstein.bernie.ses.project.out.ListProjectResponsePacketData;
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

    @Discoverer(packetID = "ListProjectPacketData", datatype = ListProjectPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<ListProjectPacketData> listProjectHandler = (data, endpoint, socket, packet, server) -> {
        final IProjectModule projectModule = ton.projectModule();
        final List<ProjectData> projects = projectModule.getProjectsFromOwner(Shortcut.useSLI(endpoint).getSessionID());
        endpoint.respond(new ListProjectResponsePacketData(projects), packet.getId());
    };

    @Discoverer(packetID = "CheckProjectExistenceRequestPacketData", datatype = CheckProjectExistenceRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<CheckProjectExistenceRequestPacketData> checkProjectExistenceHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final UUID ownerUUID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final IProjectModule projectModule = ton.projectModule();
        final List<ProjectData> projects = projectModule.getProjectsFromOwner(ownerUUID);
        final boolean match = projects.stream().anyMatch(pd -> pd.getTitle().equalsIgnoreCase(data.getTitle()));
        endpoint.respond(new CheckProjectExistenceResponsePacketData(match), packet.getId());
    };

    @Discoverer(packetID = "ProjectCreateRequestPacketData", datatype = ProjectCreateRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<CheckProjectExistenceRequestPacketData> createProjectHandler = (data, endpoint, socket, packet, server) -> {

    };
}
