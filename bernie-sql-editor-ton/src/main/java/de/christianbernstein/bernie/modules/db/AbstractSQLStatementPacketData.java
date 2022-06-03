package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import lombok.*;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractSQLStatementPacketData extends PacketData {

    private SessionCommandType type;

    private String raw;

    private Map<String, String> attributes;

    private String dbID;

}
