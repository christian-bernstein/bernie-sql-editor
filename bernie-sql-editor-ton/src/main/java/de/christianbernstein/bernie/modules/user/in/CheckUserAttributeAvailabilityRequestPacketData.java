package de.christianbernstein.bernie.modules.user.in;

import de.christianbernstein.bernie.modules.user.AttributeType;
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
@PacketMeta(dataID = "CheckUserAttributeAvailabilityRequestPacketData", protocol = "core")
public class CheckUserAttributeAvailabilityRequestPacketData extends PacketData {

    private AttributeType type;

    private String attribute;

}
