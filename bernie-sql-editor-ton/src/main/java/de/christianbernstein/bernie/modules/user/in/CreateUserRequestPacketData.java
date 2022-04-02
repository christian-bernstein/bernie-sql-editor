package de.christianbernstein.bernie.modules.user.in;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "CheckUserAttributeAvailabilityRequestPacketData", protocol = "core")
public class CreateUserRequestPacketData extends PacketData {

    private String username;

    private String firstname;

    private String lastname;

    private String email;

    private String password;

}
