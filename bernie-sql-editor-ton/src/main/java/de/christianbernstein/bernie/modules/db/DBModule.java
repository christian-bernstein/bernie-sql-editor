/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.modules.db.out.SQLCommandUpdateResponsePacketData;
import de.christianbernstein.bernie.modules.project.IProjectTaskContext;
import de.christianbernstein.bernie.modules.project.ProjectTask;
import de.christianbernstein.bernie.modules.project.ProjectTaskStatus;
import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.modules.session.ClientType;
import de.christianbernstein.bernie.modules.user.UserData;
import de.christianbernstein.bernie.ses.annotations.RegisterEventClass;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.modules.db.in.SessionCommandPacketData;
import de.christianbernstein.bernie.modules.db.in.SqlCommandStreamRequestPacketData;
import de.christianbernstein.bernie.modules.db.out.SQLCommandQueryResponsePacketData;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.SocketPreShutdownEvent;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.SocketServerLane;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.event.EventAPI;
import de.christianbernstein.bernie.sdk.misc.SerializedException;
import de.christianbernstein.bernie.sdk.misc.Utils;
import de.christianbernstein.bernie.sdk.module.IEngine;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.sdk.union.EventListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Bernstein
 */
@Accessors(fluent = true)
@RegisterEventClass
public class DBModule implements IDBModule {

    private static Optional<DBModule> instance = Optional.empty();

    /**
     * Add the client to the sqlCommandStreamConnectionLookup-map
     * todo handle any disconnecting clients -> remove them from the lookup
     */
    @Discoverer(packetID = "SqlCommandStreamRequestPacketData", datatype = SqlCommandStreamRequestPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<SqlCommandStreamRequestPacketData> sqlCommandStreamRequestHandler = (data, endpoint, socket, packet, server) -> {
        final String projectID = data.getProjectID();
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final UUID sessionID = sli.getSessionID();
        instance.ifPresent(module -> {
            final DBListenerID id = new DBListenerID(sessionID, DBListenerType.SOCKET, new HashMap<>());
            if (module.sqlListenerLookup.containsKey(projectID)) {
                final List<DBListenerID> connections = module.sqlListenerLookup.get(projectID);

                if (connections.stream().noneMatch(bdID -> bdID.id().equals(sessionID))) {
                    connections.add(id);
                }
            } else {
                final List<DBListenerID> connections = new ArrayList<>();
                connections.add(id);
                module.sqlListenerLookup.put(projectID, connections);
            }
        });
    };

    /**
     * todo add method to handle virtual connections
     */
    @Override
    public void broadcastDBPacket(@NonNull String databaseID, @NonNull PacketData data, Class<? extends PacketData> type, IDocument<Document> params) {
        final List<DBListenerID> listeningConnections = this.sqlListenerLookup().get(databaseID);
        listeningConnections.forEach(id -> {
            switch (id.type()) {
                case SOCKET -> DBModule.ton.orElseThrow().netModule().getSocketServer().getSessionManager().getSessions().forEach(ssl -> {
                    final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(ssl);
                    if (sli != null) {
                        if (sli.getSessionID().equals(id.id())) {
                            ssl.push(data);
                        }
                    } else {
                        System.err.printf("SLI of SSL '%s' is null %n", ssl.getId());
                    }
                });
                case VIRTUAL -> {
                    throw new UnsupportedOperationException("VIRTUAL connections not handled yet");
                }
            }
        });
    }

    @Override
    public void broadcastPullResponsePacket(@NonNull String databaseID, @NonNull SQLCommandQueryResponsePacketData responsePacket) {
        this.broadcastDBPacket(databaseID, responsePacket, SQLCommandQueryResponsePacketData.class, Document.empty());
    }

    @Override
    public void broadcastPushResponsePacket(@NonNull String databaseID, @NonNull SQLCommandUpdateResponsePacketData responsePacket) {
        this.broadcastDBPacket(databaseID, responsePacket, SQLCommandUpdateResponsePacketData.class, Document.empty());
    }

    @Override
    public DBQueryResult executeQuery(Connection connection, String raw) {
        final AtomicReference<ResultSet> query = new AtomicReference<>();
        final List<SerializedException> exceptions = new ArrayList<>();
        final AtomicBoolean success = new AtomicBoolean(true);
        Duration epd = null;
        try {
            epd = Utils.durationMonitoredExecution(() -> {
                try {
                    query.set(connection.prepareStatement(raw).executeQuery());
                } catch (final SQLException e) {
                    success.set(false);
                    exceptions.add(DBModule.serializeSQLException(e));
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            success.set(false);
            exceptions.add(SerializedException.builder().type(e.getClass().getName()).message(e.getMessage()).build());
        }
        return DBQueryResult.builder().result(query.get()).epd(epd).exceptions(exceptions).success(success.get()).build();
    }

    @Override
    public DBUpdateResult executeUpdate(Connection connection, String raw) {
        final List<SerializedException> exceptions = new ArrayList<>();
        final AtomicBoolean success = new AtomicBoolean(true);
        final AtomicInteger code = new AtomicInteger(-1);
        final AtomicInteger affected = new AtomicInteger(-1);
        Duration epd = null;
        try {
            epd = Utils.durationMonitoredExecution(() -> {
                try {
                    final PreparedStatement statement = connection.prepareStatement(raw);
                    code.set(statement.executeUpdate());
                    affected.set(statement.getUpdateCount());
                } catch (final SQLException e) {
                    success.set(false);
                    exceptions.add(DBModule.serializeSQLException(e));
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            success.set(false);
            exceptions.add(SerializedException.builder().type(e.getClass().getName()).message(e.getMessage()).build());
        }
        return DBUpdateResult.builder().epd(epd).code(code.get()).affectedRows(affected.get()).exceptions(exceptions).success(success.get()).build();
    }

    @Override
    public DBCommandError generatePullError(UserData ud, @NotNull DBQueryResult result, Map<String, Object> appendix) {
        final String pullErrorTitle = "SQL pull instruction faulty";
        final List<SerializedException> exceptions = result.getExceptions();
        final Map<String, Object> finalAppendix = appendix == null ? new HashMap<>() : appendix;
        return DBCommandError.builder()
                .success(result.isSuccess()).commandType(SessionCommandType.PULL)
                .exceptions(exceptions).id(UUID.randomUUID().toString())
                .title(pullErrorTitle).data(finalAppendix)
                .client(Client.builder()
                        .type(ClientType.USER)
                        .id(ud.getId())
                        .username(ud.getUsername())
                        .build())
                .timestamp(new Date()).message(String.format(
                        "%s arose while processing SQL query **(pull instruction)**. Engine processing time: %sms.",
                        exceptions.size() > 1 ? String.format("%s errors", exceptions.size()) : "One error",
                        result.getEpd().toMillis()
                ))
                .build();
    }

    @Override
    public DBCommandError generatePushError(UserData ud, @NotNull DBUpdateResult result, Map<String, Object> appendix) {
        final String pullErrorTitle = "SQL push instruction faulty or engine error";
        final List<SerializedException> exceptions = result.getExceptions();
        final Map<String, Object> finalAppendix = appendix == null ? new HashMap<>() : appendix;
        return DBCommandError.builder()
                .success(result.isSuccess()).commandType(SessionCommandType.PULL)
                .exceptions(exceptions).id(UUID.randomUUID().toString())
                .title(pullErrorTitle).data(finalAppendix)
                .client(Client.builder()
                        .type(ClientType.USER)
                        .id(ud.getId())
                        .username(ud.getUsername())
                        .build())
                .timestamp(new Date()).message(String.format(
                        "%s arose while processing SQL update. Engine processing time: %sms.",
                        exceptions.size() > 1 ? String.format("%s errors", exceptions.size()) : "One error",
                        result.getEpd().toMillis()
                ))
                .build();
    }

    @Override
    public File getDatabaseFile(@NonNull String databaseID) {
        return new File(this.config.baseDirectory() + databaseID + ".mv.db");
    }

    @Override
    public long getDatabaseAbsoluteSizeInBytes(@NonNull String databaseID) {
        try {
            return Files.size(Paths.get(this.config.baseDirectory() + databaseID + ".mv.db"));
        } catch (IOException ignored) {
            return 0;
        }
    }

    // todo make information better
    // todo inform client about the task update
    private static void doPull(UserData ud, @NotNull IDatabaseAccessPoint db, SessionCommandPacketData data, SocketServerLane endpoint) {
        db.session(session -> session.doWork(connection -> {
            final IDBModule module = DBModule.ton.orElseThrow().dbModule();
            final String databaseID = data.getDbID();
            final IProjectTaskContext task = DBModule.ton.orElseThrow().projectModule().createTask(ProjectTask.builder().status(ProjectTaskStatus.RUNNING).taskID(UUID.randomUUID().toString()).data(new HashMap<>()).title("pull command").type("pull").timestamp(new Date()).projectID(databaseID).build());
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
            module.broadcastPullResponsePacket(databaseID, responsePacket);
        }));
    }

    private static void doPush(UserData ud, @NonNull IDatabaseAccessPoint db, SessionCommandPacketData data, SocketServerLane endpoint) {
        db.session(session -> session.doWork(connection -> {
            final IDBModule module = DBModule.ton.orElseThrow().dbModule();
            final String raw = data.getRaw();
            final String databaseID = data.getDbID();
            final DBUpdateResult result = module.executeUpdate(connection, raw);
            final boolean success = result.isSuccess();
            final DBCommandError error = success ? null : module.generatePushError(ud, result, new HashMap<>());
            final Client client = Client.builder().type(ClientType.USER).id(ud.getId()).username(ud.getUsername()).build();
            final SQLCommandUpdateResponsePacketData responsePacket = SQLCommandUpdateResponsePacketData.builder().timestamp(new Date()).affected(result.getAffectedRows()).client(client).errormessage("").code(result.getCode()).success(success).error(error).sql(raw).databaseID(databaseID).durationMS(result.getEpd().toMillis()).build();
            module.broadcastPushResponsePacket(databaseID, responsePacket);
        }));
    }

    @Discoverer(packetID = "SessionCommandPacketData", datatype = SessionCommandPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<SessionCommandPacketData> commandHandler = (data, endpoint, socket, packet, server) -> {
        final IDBModule module = DBModule.ton.orElseThrow().dbModule();
        final IDatabaseAccessPoint db = module.loadDatabase(data.getDbID(), DatabaseAccessPointLoadConfig.builder().build());

        final ITon instance = DBModule.ton.orElseThrow();
        final UserData ud = instance.userModule().getUserDataOfUsername(Shortcut.useUserSession(endpoint).getCredentials().getUsername());

        switch (data.getType()) {
            case PULL -> DBModule.doPull(ud, db, data, endpoint);
            case PUSH -> DBModule.doPush(ud, db, data, endpoint);
        }
    };

    private static SerializedException serializeSQLException(@NotNull SQLException e) {
        final int errorCode = e.getErrorCode();
        final String state = e.getSQLState();
        final String message = e.getMessage();
        return SerializedException.builder()
                .type(e.getClass().getName())
                .message(message)
                .data(Map.of(
                        "state", state,
                        "errorCode", errorCode))
                .build();
    }

    private static Optional<ITon> ton = Optional.empty();

    private final List<IDatabaseAccessPoint> activeDatabases = new ArrayList<>();

    private final Configuration rootConfiguration = new Configuration();

    private final DBConfig config = DBConfig.builder().build();

    /**
     * todo rename variable to sqlListenerLookup
     *
     * todo replace UUID with better datatype -> {
     *      id, type: VIRTUAL | DEVICE
     *  }
     * <p>
     * todo make english great again c.c
     * Contains a list of databases (NOT sessions) and their related connections,
     * who listen on sql commands happening to the databases.
     */
    @Getter
    private final Map<String, List<DBListenerID>> sqlListenerLookup = new HashMap<>();

    private SessionFactory rootSessionFactory;

    /**
     * todo implement loose socket closing detection
     * this method will only be called if a certain amount of keep-alive packets failed
     * As described here https://stackoverflow.com/questions/10240694/java-socket-api-how-to-tell-if-a-connection-has-been-closed#:~:text=isConnected()%20tells%20you%20whether,you%20have%2C%20it%20returns%20false
     * TCP doesn't now the state of its connection -> there is no way to determine whether a client has disconnected or not
     * <p>
     * Quote:
     * " On the contrary: it was deliberate. Previous protocol suites such as SNA had a 'dial tone'.
     * TCP was designed to survive a nuclear war, and, more trivially, router downs and ups: hence the complete
     * absence of anything like a dial tone, connection status, etc.;
     * and it is also why TCP keepalive is described in the RFCs as a controversial feature, and why it is always off by default.
     * TCP is still with us. SNA? IPX? ISO? Not. They got it right. "
     * by https://stackoverflow.com/users/207421/user207421
     */
    @EventListener
    private static void onSessionClose() {
    }

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IDBModule.super.boot(api, module, manager);
        DBModule.instance = Optional.of(this);
        DBModule.ton = Optional.of(api);
        this.registerSocketCloseEventHandler();
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IDBModule.super.uninstall(api, module, manager);
        this.activeDatabases.forEach(IDatabaseAccessPoint::unload);
        this.ton = Optional.empty();
    }

    @Override
    public IDatabaseAccessPoint loadDatabase(String dbID, @NotNull DatabaseAccessPointLoadConfig config) {
        final Optional<IDatabaseAccessPoint> opt = this.activeDatabases.stream().filter(db -> db.getID().equals(dbID)).findFirst();
        final AtomicReference<IDatabaseAccessPoint> ref = new AtomicReference<>();
        opt.ifPresentOrElse(ref::set, () -> {
            // todo fire event
            // The database isn't loaded yet, load it
            final Configuration hibernateConfig = new Configuration()
                    .setProperty("hibernate.connection.driver_class", config.getDriver())
                    .setProperty("hibernate.connection.url", "jdbc:h2:" + this.config.baseDirectory() + dbID)
                    .setProperty("hibernate.connection.username", config.getUsername())
                    .setProperty("hibernate.connection.password", config.getPassword())
                    .setProperty("hibernate.dialect", config.getDialect())
                    .setProperty("hibernate.hbm2ddl.auto", config.getDdlMode().getHbnPropertyVal());
            final DatabaseAccessPoint database = new DatabaseAccessPoint(dbID, hibernateConfig);
            this.activeDatabases.add(database);
            ref.set(database);
        });
        return ref.get();
    }

    @Override
    public boolean unloadDatabase(String dbID) {
        final Optional<IDatabaseAccessPoint> database = this.activeDatabases.stream().filter(db -> db.getID().equals(dbID)).findFirst();
        database.ifPresent(IDatabaseAccessPoint::unload);
        if (database.isPresent()) {
            final IDatabaseAccessPoint dbObj = database.get();
            this.activeDatabases.remove(dbObj);
        }
        return database.isPresent();
    }

    @Override
    public String copyDatabase(String dbID) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public List<IDatabaseAccessPoint> activeDatabases() {
        return this.activeDatabases;
    }

    @Override
    public Configuration getRootConfiguration() {
        return this.rootConfiguration;
    }

    @Override
    public SessionFactory getRootSessionFactory() {
        return this.rootSessionFactory;
    }

    @Override
    public @NonNull IDBModule me() {
        return this;
    }

    private void registerSocketCloseEventHandler() {
        ton.orElseThrow().netModule().getSocketServer().getEventController().registerHandler(new EventAPI.Handler<>(SocketPreShutdownEvent.class, (socketPreShutdownEvent, iDocument) -> {
            final SocketServerLane lane = socketPreShutdownEvent.session();
            final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(lane);
            if (sli != null && sli.getSessionID() != null) {
                this.sqlListenerLookup.forEach((databaseID, listeners) -> listeners.removeIf(id ->
                        id.type().equals(DBListenerType.SOCKET) && id.id().equals(sli.getSessionID())
                ));
            }
        }));
    }
}
