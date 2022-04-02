package de.christianbernstein.bernie.modules.project.in;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "ProjectCreateRequestPacketData", protocol = Constants.centralProtocolName)
public class ProjectCreateRequestPacketData extends PacketData {

    private String title;

    private String description;

    private boolean stator;

    private String dbFactoryID;

    private Map<String, Object> dbFactoryParams;

}
