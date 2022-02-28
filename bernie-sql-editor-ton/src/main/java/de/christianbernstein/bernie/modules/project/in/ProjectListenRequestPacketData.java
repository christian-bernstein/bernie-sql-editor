package de.christianbernstein.bernie.modules.project.in;

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
 * Client requests the server to send all actions happening inside the db project
 * The server will notify the client if anything has changed in the project like load status, name, contributor list,
 * file size, sql command history
 *
 * @author Christian Bernstein
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@PacketMeta(dataID = "ProjectListenRequestPacketData", protocol = Constants.centralProtocolName)
@AllArgsConstructor
public class ProjectListenRequestPacketData extends PacketData {

    private String projectID;

}
