package de.christianbernstein.bernie.modules.project.out;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.*;
import org.jetbrains.annotations.Nullable;

/**
 * todo implement in client
 *
 * @author Christian Bernstein
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PacketMeta(dataID = "ProjectCreationErrorPacketData", protocol = Constants.centralProtocolName)
public class ProjectCreationErrorPacketData extends PacketData {

    private String errorType;

    private String errorMessage;

    @Nullable
    private Exception exception;
}
