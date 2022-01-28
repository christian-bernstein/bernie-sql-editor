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

package de.christianbernstein.bernie.ses.db;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.db.in.SessionCommandPacketData;
import de.christianbernstein.bernie.shared.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.shared.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.NonNull;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("all")
public class DBModule implements IDBModule {

    private static Optional<DBModule> instance = Optional.empty();

    private static Optional<ITon> ton = Optional.empty();

    private static Map<String, List<String>> sessionConnectionLookup = new HashMap<>();

    @Discoverer(packetID = "SessionCommandPacketData", datatype = SessionCommandPacketData.class, protocols = Constants.centralProtocolName)
    private static final IPacketHandlerBase<SessionCommandPacketData> commandHandler = (data, endpoint, socket, packet, server) -> {
        System.out.println("handle SessionCommandPacketData");
        DBModule.instance.ifPresentOrElse(module -> {
            System.out.println("Got module!");
            final IDatabaseAccessPoint db = module.loadDatabase(data.getDbID(), DatabaseAccessPointLoadConfig.builder().build());
            switch (data.getType()) {
                case PULL -> {
                    System.out.println("pull");
                    db.session(session -> session.doWork(connection -> {
                        System.out.println("Do work with connection");
                        final String raw = data.getRaw();
                        System.out.printf("Raw sql command: '%s'%n", raw);
                        final ResultSet query = connection.prepareStatement(raw).executeQuery();
                        System.err.println("pull list:");
                        DBUtilities.resultSetToList(query).forEach(document -> {
                            System.err.println("-> " + document.toSlimString());
                        });



                    }));
                    break;
                }
                case PUSH -> {
                    System.out.println("push");
                    db.session(session -> session.doWork(connection -> {
                        final String raw = data.getRaw();
                        final int update = connection.prepareStatement(raw).executeUpdate();
                        System.err.println("update: " + update);
                    }));
                    break;
                }
            }
        }, () -> {
            System.err.println("No module present");
        });
    };

    private final List<IDatabaseAccessPoint> activeDatabases = new ArrayList<>();

    private final Configuration rootConfiguration = new Configuration();

    private final DBConfig config = DBConfig.builder().build();

    private SessionFactory rootSessionFactory;

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IDBModule.super.boot(api, module, manager);
        DBModule.instance = Optional.of(this);
        DBModule.ton = Optional.of(api);
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        IDBModule.super.uninstall(api, module, manager);
        this.ton = null;
    }

    @Override
    public IDatabaseAccessPoint loadDatabase(String dbID, @NotNull DatabaseAccessPointLoadConfig config) {
        final Optional<IDatabaseAccessPoint> opt = this.activeDatabases.stream().filter(db -> db.getID().equals(dbID)).findFirst();
        final AtomicReference<IDatabaseAccessPoint> ref = new AtomicReference<>();
        opt.ifPresentOrElse(ref::set, () -> {
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
}
