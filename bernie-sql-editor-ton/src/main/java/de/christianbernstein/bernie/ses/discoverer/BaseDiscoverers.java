package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.modules.net.in.PingPacketData;
import de.christianbernstein.bernie.modules.net.out.PongPacketData;
import de.christianbernstein.bernie.modules.settings.in.SettingsRequestPacketData;
import de.christianbernstein.bernie.modules.settings.out.SettingsResponsePacketData;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.modules.cdn.CDNRequest;
import de.christianbernstein.bernie.modules.cdn.CDNResponse;
import de.christianbernstein.bernie.modules.cdn.ICDNModule;
import de.christianbernstein.bernie.modules.cdn.in.CDNRequestPacketData;
import de.christianbernstein.bernie.modules.cdn.out.CDNResponsePacketData;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.modules.net.in.RequestServerFootprintPacketData;
import de.christianbernstein.bernie.modules.user.IUserModule;
import de.christianbernstein.bernie.modules.user.UserCreationResult;
import de.christianbernstein.bernie.modules.user.UserData;
import de.christianbernstein.bernie.modules.user.in.CheckUserAttributeAvailabilityRequestPacketData;
import de.christianbernstein.bernie.modules.user.in.CreateUserRequestPacketData;
import de.christianbernstein.bernie.modules.user.out.CheckUserAttributeAvailabilityResponsePacketData;
import de.christianbernstein.bernie.modules.user.out.CreateUserResponsePacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class BaseDiscoverers {

    @UseTon
    private ITon ton;

    private final IPacketHandlerBase<RequestServerFootprintPacketData> footprintHandler = (data, endpoint, socket, packet, server) -> {

    };

    @Discoverer(packetID = "PingPacketData", datatype = PingPacketData.class, protocols =  Constants.coreProtocolName)
    private final IPacketHandlerBase<PingPacketData> pingHandler = (data, endpoint, socket, packet, server) -> {
        packet.respond(new PongPacketData(data.getOutboundTimestamp(), System.currentTimeMillis()), endpoint);
    };

    @Discoverer(packetID = "CheckUserAttributeAvailabilityRequestPacketData", datatype = CheckUserAttributeAvailabilityRequestPacketData.class, protocols =  Constants.coreProtocolName)
    private final IPacketHandlerBase<CheckUserAttributeAvailabilityRequestPacketData> checkUserAttributeAvailabilityHandler = (data, endpoint, socket, packet, server) -> {
        switch (data.getType()) {
            case EMAIL -> throw new UnsupportedOperationException("Email CheckUserAttributeAvailabilityRequestPacketData handler not implemented");
            case USERNAME -> {
                final IUserModule userModule = ton.userModule();
                final UserData ud = userModule.getUserDataOfUsername(data.getAttribute());
                packet.respond(new CheckUserAttributeAvailabilityResponsePacketData(ud == null), endpoint);
            }
        }
    };

    @Discoverer(packetID = "CreateUserRequestPacketData", datatype = CreateUserRequestPacketData.class, protocols =  Constants.coreProtocolName)
    private final IPacketHandlerBase<CreateUserRequestPacketData> createUserHandler = (data, endpoint, socket, packet, server) -> {
        final IUserModule userModule = ton.userModule();
        final Date creationData = new Date();
        final UserCreationResult result = userModule.plainCreateAccount(UserData.builder()
                .id(UUID.randomUUID().toString())
                .email(data.getEmail())
                .username(data.getUsername())
                .password(data.getPassword())
                .firstname(data.getFirstname())
                .lastname(data.getLastname())
                .userEntrySetupDate(creationData)
                .lastActive(creationData)
                .build());
        switch (result) {
            case OK -> packet.respond(new CreateUserResponsePacketData(true, result), endpoint);
            case UUID_ALREADY_TAKEN, USERNAME_ALREADY_TAKEN, INTERNAL_ERROR -> packet.respond(new CreateUserResponsePacketData(false, result), endpoint);
        }
    };

    @Discoverer(packetID = "CDNRequestPacketData", datatype = CDNRequestPacketData.class, protocols = Constants.coreProtocolName)
    private final IPacketHandlerBase<CDNRequestPacketData> cdnRequestHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String viewerID = sli != null ? ton.getUserFromSessionID(sli.getSessionID()).getID() : null;
        final CDNRequest request = new CDNRequest(viewerID, data.getBranches());
        final ICDNModule module = ton.cdnModule();
        final CDNResponse response = module.request(request);
        packet.respond(new CDNResponsePacketData(response), endpoint);
    };

    @Discoverer(packetID = "SettingsRequestPacketData", datatype = SettingsRequestPacketData.class, protocols = Constants.coreProtocolName)
    private final IPacketHandlerBase<SettingsRequestPacketData> settingsRequestHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String viewerID = sli != null ? ton.getUserFromSessionID(sli.getSessionID()).getID() : null;

        System.out.println("SettingsRequestPacketData :: " + data);

        switch (data.getCompoundID()) {
            case "a" -> {
                packet.respond(new SettingsResponsePacketData(Map.of("value", "Hello world")), endpoint);
            }
            case "b" -> {
                packet.respond(new SettingsResponsePacketData(Map.of(
                        "age", 19,
                        "id", UUID.randomUUID().toString(),
                        "description", "Hello world!"
                )), endpoint);
            }
        }

    };
}
