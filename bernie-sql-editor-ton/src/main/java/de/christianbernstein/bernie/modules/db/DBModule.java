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

import de.christianbernstein.bernie.modules.project.ProjectTask;
import de.christianbernstein.bernie.modules.project.ProjectTaskStatus;
import de.christianbernstein.bernie.modules.session.Client;
import de.christianbernstein.bernie.modules.session.ClientType;
import de.christianbernstein.bernie.ses.annotations.RegisterEventClass;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import de.christianbernstein.bernie.modules.db.in.SessionCommandPacketData;
import de.christianbernstein.bernie.modules.db.in.SqlCommandStreamRequestPacketData;
import de.christianbernstein.bernie.modules.db.out.SQLCommandQueryResponsePacketData;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketPreShutdownEvent;
import de.christianbernstein.bernie.shared.discovery.websocket.server.SocketServerLane;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.event.EventAPI;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.union.EventListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.*;
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
        // todo remove debug
        System.err.println("SqlCommandStreamRequestPacketData received!!");

        final String projectID = data.getProjectID();
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final UUID sessionID = sli.getSessionID();
        instance.ifPresent(module -> {
            final DBListenerID id = new DBListenerID(sessionID, DBListenerType.SOCKET);
            if (module.sqlCommandStreamConnectionLookup.containsKey(projectID)) {
                final List<DBListenerID> connections = module.sqlCommandStreamConnectionLookup.get(projectID);

                if (connections.stream().noneMatch(bdID -> bdID.id().equals(sessionID))) {
                    connections.add(id);
                }
            } else {
                final List<DBListenerID> connections = new ArrayList<>();
                connections.add(id);
                module.sqlCommandStreamConnectionLookup.put(projectID, connections);
            }
        });
    };

    // todo investigate bug -> sli might not work properly
    //
    @Discoverer(packetID = "SessionCommandPacketData", datatype = SessionCommandPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<SessionCommandPacketData> commandHandler = (data, endpoint, socket, packet, server) -> {
        final IDBModule module = DBModule.ton.orElseThrow().dbModule();
        final IDatabaseAccessPoint db = module.loadDatabase(data.getDbID(), DatabaseAccessPointLoadConfig.builder().build());
        switch (data.getType()) {
            case PULL -> db.session(session -> session.doWork(connection -> {
                final String databaseID = data.getDbID();

                // todo make information better
                DBModule.ton.orElseThrow().projectModule().createTask(ProjectTask.builder()
                        .status(ProjectTaskStatus.RUNNING)
                        .taskID(UUID.randomUUID().toString())
                        .data(new HashMap<>())
                        .title("pull command")
                        .type("pull")
                        .timestamp(new Date())
                        .projectID(databaseID)
                        .build());

                final String raw = data.getRaw();
                // todo time this function and send the duration back to the client
                final ResultSet query = connection.prepareStatement(raw).executeQuery();
                final List<Document> set = DBUtilities.resultSetToList(query);
                final List<DBListenerID> listeningConnections = module.sqlCommandStreamConnectionLookup().get(data.getDbID());
                final Client client = Client.builder().type(ClientType.USER).id("implement..").username("implement..").build();
                final String errormessage = "implement..";
                final List<Column> columns = new ArrayList<>();
                final List<Row> rows = new ArrayList<>();
                final boolean success = true;

                // todo make more versatile solution -> if result empty -> no columns
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

                listeningConnections.forEach(id -> {
                    switch (id.type()) {
                        case SOCKET -> {
                            DBModule.ton.orElseThrow().netModule().getSocketServer().getSessionManager().getSessions().forEach(ssl -> {
                                System.err.println("checking ssl: " + ssl.getId());
                                final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(ssl);
                                if (sli != null) {
                                    if (sli.getSessionID().equals(id.id())) {
                                        System.err.println("pushing to ssl:" + ssl);
                                        ssl.push(SQLCommandQueryResponsePacketData.builder()
                                                .databaseID(databaseID)
                                                .columns(columns)
                                                .rows(rows)
                                                .errormessage(errormessage)
                                                .sql(raw)
                                                .success(success)
                                                .client(client)
                                                .timestamp(new Date())
                                                .build());
                                    }
                                } else {
                                    System.err.printf("SLI of SSL '%s' is null %n", ssl.getId());
                                }
                            });
                        }
                        case VIRTUAL -> {
                            // todo add method to handle virtual connections
                        }
                    }
                });
            }));
            case PUSH -> {
                System.out.println("push");
                db.session(session -> session.doWork(connection -> {
                    final String raw = data.getRaw();
                    final int update = connection.prepareStatement(raw).executeUpdate();
                    System.err.println("update: " + update);
                }));
            }
        }
    };

    private static Optional<ITon> ton = Optional.empty();

    private final List<IDatabaseAccessPoint> activeDatabases = new ArrayList<>();

    private final Configuration rootConfiguration = new Configuration();

    private final DBConfig config = DBConfig.builder().build();

    /**
     * todo replace UUID with better datatype -> {
     *      id, type: VIRTUAL | DEVICE
     *  }
     * <p>
     * todo make english great again c.c
     * Contains a list of databases (NOT sessions) and their related connections,
     * who listen on sql commands happening to the databases.
     */
    @Getter
    private final Map<String, List<DBListenerID>> sqlCommandStreamConnectionLookup = new HashMap<>();

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
                this.sqlCommandStreamConnectionLookup.forEach((databaseID, listeners) -> listeners.removeIf(id ->
                        id.type().equals(DBListenerType.SOCKET) && id.id().equals(sli.getSessionID())
                ));
            }
        }));
    }
}
