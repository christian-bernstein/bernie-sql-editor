package de.christianbernstein.bernie.ses.db.out;

import de.christianbernstein.bernie.ses.session.Client;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.*;

/**
 * todo add affected rows count
 *
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@PacketMeta(dataID = "SQLCommandUpdateResponsePacketData", protocol = Constants.centralProtocolName)
@AllArgsConstructor
public class SQLCommandUpdateResponsePacketData extends PacketData {

    private String sql;

    private Client client;

    private String databaseID;

    private int code;

    private boolean success;

    private String errormessage;
}
