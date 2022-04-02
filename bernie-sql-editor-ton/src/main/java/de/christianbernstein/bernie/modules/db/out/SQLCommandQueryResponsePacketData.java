package de.christianbernstein.bernie.modules.db.out;

import de.christianbernstein.bernie.modules.db.Column;
import de.christianbernstein.bernie.modules.db.DBCommandError;
import de.christianbernstein.bernie.modules.db.Row;
import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketMeta;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * todo add processing duration
 * todo add affected rows count
 *
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@PacketMeta(dataID = "SQLCommandQueryResponsePacketData", protocol = Constants.centralProtocolName)
@AllArgsConstructor
public class SQLCommandQueryResponsePacketData extends PacketData {

    private String sql;

    private Client client;

    private String databaseID;

    private boolean success;

    private String errormessage;

    private List<Column> columns;

    private List<Row> rows;

    private Date timestamp;

    private DBCommandError error;

    private long durationMS;
}
