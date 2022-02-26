package de.christianbernstein.bernie.ses.user.out;

import de.christianbernstein.bernie.ses.user.UserCreationResult;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
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
