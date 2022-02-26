package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.net.in.RequestServerFootprintPacketData;
import de.christianbernstein.bernie.ses.user.*;
import de.christianbernstein.bernie.ses.user.in.CheckUserAttributeAvailabilityRequestPacketData;
import de.christianbernstein.bernie.ses.user.in.CreateUserRequestPacketData;
import de.christianbernstein.bernie.ses.user.out.CheckUserAttributeAvailabilityResponsePacketData;
import de.christianbernstein.bernie.ses.user.out.CreateUserResponsePacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import lombok.experimental.UtilityClass;

import java.util.Date;
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

    @Discoverer(packetID = "CheckUserAttributeAvailabilityRequestPacketData", datatype = CheckUserAttributeAvailabilityRequestPacketData.class, protocols = "base")
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

    @Discoverer(packetID = "CreateUserRequestPacketData", datatype = CreateUserRequestPacketData.class, protocols = "base")
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
}
