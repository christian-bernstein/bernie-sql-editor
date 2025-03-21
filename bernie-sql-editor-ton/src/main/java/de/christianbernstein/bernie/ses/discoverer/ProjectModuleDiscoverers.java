package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.modules.project.in.*;
import de.christianbernstein.bernie.modules.project.out.*;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.modules.project.IProjectModule;
import de.christianbernstein.bernie.modules.project.ProjectAlreadyExistException;
import de.christianbernstein.bernie.modules.project.ProjectCreationData;
import de.christianbernstein.bernie.modules.project.ProjectData;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
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

    /**
     * todo check if the user can see the project
     */
    @Discoverer(packetID = "ProjectFileSizeRequestPacketData", datatype = ProjectFileSizeRequestPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<ProjectFileSizeRequestPacketData> projectSizeHandler = (data, endpoint, socket, packet, server) -> {
        final long size = ton.dbModule().getDatabaseAbsoluteSizeInBytes(data.getProjectID());
        endpoint.respond(new ProjectFileSizeResponsePacketData(size), packet.getId());
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
