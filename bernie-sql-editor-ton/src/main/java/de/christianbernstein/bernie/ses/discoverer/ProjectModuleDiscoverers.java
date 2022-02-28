package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.modules.project.IProjectModule;
import de.christianbernstein.bernie.modules.project.ProjectAlreadyExistException;
import de.christianbernstein.bernie.modules.project.ProjectCreationData;
import de.christianbernstein.bernie.modules.project.ProjectData;
import de.christianbernstein.bernie.modules.project.in.CheckProjectExistenceRequestPacketData;
import de.christianbernstein.bernie.modules.project.in.ListProjectPacketData;
import de.christianbernstein.bernie.modules.project.in.ProjectCreateRequestPacketData;
import de.christianbernstein.bernie.modules.project.in.ProjectDeleteRequestPacketData;
import de.christianbernstein.bernie.modules.project.out.CheckProjectExistenceResponsePacketData;
import de.christianbernstein.bernie.modules.project.out.ListProjectResponsePacketData;
import de.christianbernstein.bernie.modules.project.out.ProjectCreateResponsePacketData;
import de.christianbernstein.bernie.modules.project.out.ProjectCreationErrorPacketData;
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
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String ownerUUID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final List<ProjectData> projects = projectModule.getProjectsFromOwner(ownerUUID);
        endpoint.respond(new ListProjectResponsePacketData(projects), packet.getId());
    };

    @Discoverer(packetID = "CheckProjectExistenceRequestPacketData", datatype = CheckProjectExistenceRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<CheckProjectExistenceRequestPacketData> checkProjectExistenceHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String ownerUUID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final IProjectModule projectModule = ton.projectModule();
        final List<ProjectData> projects = projectModule.getProjectsFromOwner(ownerUUID);
        final boolean match = projects.stream().anyMatch(pd -> pd.getTitle().equalsIgnoreCase(data.getTitle()));
        endpoint.respond(new CheckProjectExistenceResponsePacketData(match), packet.getId());
    };

    @Discoverer(packetID = "ProjectDeleteRequestPacketData", datatype = ProjectDeleteRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<ProjectDeleteRequestPacketData> deleteProjectHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final IProjectModule projectModule = ton.projectModule();
        projectModule.deleteProject(UUID.fromString(data.getId()));
    };

    /**
     * todo add project password and security level -> private access, intermediate access (group administrators can see)
     * todo add permission checking
     * todo if stator -> start the database
     */
    @Discoverer(packetID = "ProjectCreateRequestPacketData", datatype = ProjectCreateRequestPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<ProjectCreateRequestPacketData> createProjectHandler = (data, endpoint, socket, packet, server) -> {
        final IProjectModule projectModule = ton.projectModule();
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String ownerUUID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final String description = data.getDescription();
        final boolean stator = data.isStator();

        try {
            projectModule.createProject(ProjectCreationData.builder()
                    .title(data.getTitle())
                    .description(description)
                    .creatorUserID(ownerUUID)
                    .dbFactoryID(data.getDbFactoryID())
                    .dbFactoryParams(data.getDbFactoryParams())
                    .stator(stator)
                    .build());

            packet.respond(new ProjectCreateResponsePacketData(), endpoint);
        } catch (final ProjectAlreadyExistException e) {
            ton.ifDebug(e::printStackTrace);
            packet.respond(new ProjectCreationErrorPacketData("ProjectAlreadyExistException", e.getMessage(), e), endpoint);
        }
    };
}
