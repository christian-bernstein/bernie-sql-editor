package de.christianbernstein.bernie.ses.discoverer;

import de.christianbernstein.bernie.modules.db.Column;
import de.christianbernstein.bernie.modules.db.DBCommandError;
import de.christianbernstein.bernie.modules.db.DBQueryResult;
import de.christianbernstein.bernie.modules.db.DBUpdateResult;
import de.christianbernstein.bernie.modules.db.DBUtilities;
import de.christianbernstein.bernie.modules.db.DatabaseAccessPointLoadConfig;
import de.christianbernstein.bernie.modules.db.IDBModule;
import de.christianbernstein.bernie.modules.db.IDatabaseAccessPoint;
import de.christianbernstein.bernie.modules.db.Row;
import de.christianbernstein.bernie.modules.db.in.IntrinsicCommandPacketData;
import de.christianbernstein.bernie.modules.db.in.SessionCommandPacketData;
import de.christianbernstein.bernie.modules.db.out.SQLCommandQueryResponsePacketData;
import de.christianbernstein.bernie.modules.db.out.SQLCommandUpdateResponsePacketData;
import de.christianbernstein.bernie.modules.project.IProjectTaskContext;
import de.christianbernstein.bernie.modules.project.ProjectTask;
import de.christianbernstein.bernie.modules.project.ProjectTaskStatus;
import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.modules.session.ClientType;
import de.christianbernstein.bernie.modules.user.UserData;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class DatabaseDiscoverers {

    @UseTon
    private ITon ton;

    @Discoverer(packetID = "IntrinsicCommandPacketData", datatype = IntrinsicCommandPacketData.class, protocols = Constants.centralProtocolName)
    private final IPacketHandlerBase<IntrinsicCommandPacketData> intrinsicCommandHandler = (data, endpoint, socket, packet, server) -> {
        final IDBModule module = ton.dbModule();
        final IDatabaseAccessPoint db = module.loadDatabase(data.getDbID(), DatabaseAccessPointLoadConfig.builder().build());
        final UserData ud = ton.userModule().getUserDataOfUsername(Shortcut.useUserSession(endpoint).getCredentials().getUsername());

        switch (data.getType()) {
            case PULL -> db.session(session -> session.doWork(connection -> {
                final String databaseID = data.getDbID();
                final IProjectTaskContext task = ton.projectModule().createTask(ProjectTask.builder().status(ProjectTaskStatus.RUNNING).taskID(UUID.randomUUID().toString()).data(new HashMap<>()).title("pull command").type("pull").timestamp(new Date()).projectID(databaseID).build());
                final String raw = data.getRaw();
                final DBQueryResult result = module.executeQuery(connection, raw);
                final boolean success = result.isSuccess();
                final long durationMS = result.getEpd().toMillis();
                final DBCommandError error = success ? null : module.generatePullError(ud, result, new HashMap<>());
                final List<Document> set = success ? DBUtilities.resultSetToList(result.getResult()) : new ArrayList<>();
                final Client client = Client.builder().type(ClientType.USER).id(ud.getId()).username(ud.getUsername()).build();
                final String errormessage = "implement..";
                final List<Column> columns = new ArrayList<>();
                final List<Row> rows = new ArrayList<>();

                // todo there seems to be a error when a cell is null

                if (set.size() > 0) {
                    final Document row = set.get(0);
                    if (row != null) {
                        row.toMap().keySet().forEach(col -> columns.add(new Column(col)));
                    }
                }

                set.forEach(document -> {
                    document.toMap();
                    final Row row1 = new Row();
                    row1.putAll(document.toMap());
                    rows.add(row1);
                });

                task.finishWithSuccess();
                final SQLCommandQueryResponsePacketData responsePacket = SQLCommandQueryResponsePacketData.builder().databaseID(databaseID).columns(columns).durationMS(durationMS).error(error).rows(rows).errormessage(errormessage).sql(raw).success(success).client(client).timestamp(new Date()).build();
                packet.respond(responsePacket, endpoint);
            }));
            case PUSH -> db.session(session -> session.doWork(connection -> {
                final String raw = data.getRaw();
                final String databaseID = data.getDbID();
                final DBUpdateResult result = module.executeUpdate(connection, raw);
                final boolean success = result.isSuccess();
                final DBCommandError error = success ? null : module.generatePushError(ud, result, new HashMap<>());
                final Client client = Client.builder().type(ClientType.USER).id(ud.getId()).username(ud.getUsername()).build();
                final SQLCommandUpdateResponsePacketData responsePacket = SQLCommandUpdateResponsePacketData.builder().timestamp(new Date()).affected(result.getAffectedRows()).client(client).errormessage("").code(result.getCode()).success(success).error(error).sql(raw).databaseID(databaseID).durationMS(result.getEpd().toMillis()).build();
                packet.respond(responsePacket, endpoint);
            }));
        }
    };
}
