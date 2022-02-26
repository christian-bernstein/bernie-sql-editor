package de.christianbernstein.bernie.ses.user.out;

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
@PacketMeta(dataID = "CheckUserAttributeAvailabilityResponsePacketData", protocol = "base")
public class CheckUserAttributeAvailabilityResponsePacketData extends PacketData {

    private boolean available;

}
