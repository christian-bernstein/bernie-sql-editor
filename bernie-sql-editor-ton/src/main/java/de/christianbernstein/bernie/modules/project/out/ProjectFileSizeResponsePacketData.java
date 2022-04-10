package de.christianbernstein.bernie.modules.project.out;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@PacketMeta(dataID = "ProjectFileSizeResponsePacketData", protocol = Constants.centralProtocolName)
public class ProjectFileSizeResponsePacketData extends PacketData {

    private long fileSize;

}
