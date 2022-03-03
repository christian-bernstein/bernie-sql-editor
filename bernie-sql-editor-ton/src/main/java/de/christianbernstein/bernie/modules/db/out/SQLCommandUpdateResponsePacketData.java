package de.christianbernstein.bernie.modules.db.out;

import de.christianbernstein.bernie.modules.db.DBCommandError;
import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.*;

import java.util.Date;

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

    private int affected;

    private int code;

    private boolean success;

    private Date timestamp;

    private String errormessage;

    private DBCommandError error;

    private long durationMS;

}
