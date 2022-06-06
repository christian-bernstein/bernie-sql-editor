package de.christianbernstein.bernie.modules.db.out;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "SessionLoadResourceResponsePacketData", protocol = Constants.centralProtocolName)
public class SessionLoadResourceResponsePacketData extends PacketData {

    private String mainEditorContent;

}
