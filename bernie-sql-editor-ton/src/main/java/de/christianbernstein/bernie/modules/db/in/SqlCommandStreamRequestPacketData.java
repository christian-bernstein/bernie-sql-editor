package de.christianbernstein.bernie.modules.db.in;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.PacketMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * todo implement functionality
 *
 * Client requests the server to send all the sql command response data
 * The data includes update return statements and query data
 * The client is then supposed to show a modal containing a table if the data was from a query request
 *
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "SqlCommandStreamRequestPacketData", protocol = Constants.centralProtocolName)
@AllArgsConstructor
public class SqlCommandStreamRequestPacketData extends PacketData {

    private String projectID;

}
