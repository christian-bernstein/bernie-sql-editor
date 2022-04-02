package de.christianbernstein.bernie.modules.user.out;

import de.christianbernstein.bernie.modules.user.UserCreationResult;
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
@PacketMeta(dataID = "CreateUserResponsePacketData", protocol = "core")
public class CreateUserResponsePacketData extends PacketData {

    private boolean success;

    private UserCreationResult result;

}
