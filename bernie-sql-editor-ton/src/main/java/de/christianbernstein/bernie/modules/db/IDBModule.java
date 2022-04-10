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

import de.christianbernstein.bernie.modules.db.out.SQLCommandQueryResponsePacketData;
import de.christianbernstein.bernie.modules.db.out.SQLCommandUpdateResponsePacketData;
import de.christianbernstein.bernie.modules.user.UserData;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.sdk.discovery.websocket.PacketData;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.misc.IFluently;
import de.christianbernstein.bernie.sdk.module.Dependency;
import de.christianbernstein.bernie.sdk.module.IBaseModuleClass;
import de.christianbernstein.bernie.sdk.module.ModuleDefinition;
import de.christianbernstein.bernie.sdk.module.Module;
import lombok.NonNull;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public interface IDBModule extends IBaseModuleClass<ITon>, IFluently<IDBModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> dbModule = Module.<ITon>builder()
            .name("db_module")
            .dependency(Dependency.builder().module("net_module").build())
            .build()
            .$(module -> module.getShardManager().install(DBModule.class));

    IDatabaseAccessPoint loadDatabase(String dbID, DatabaseAccessPointLoadConfig config);

    Map<String, List<DBListenerID>> sqlListenerLookup();

    boolean unloadDatabase(String dbID);

    List<IDatabaseAccessPoint> activeDatabases();

    String copyDatabase(String dbID);

    Configuration getRootConfiguration();

    SessionFactory getRootSessionFactory();

    void broadcastDBPacket(@NonNull String databaseID, @NonNull PacketData data, Class<? extends PacketData> type, IDocument<Document> params);

    void broadcastPullResponsePacket(@NonNull String databaseID, @NonNull SQLCommandQueryResponsePacketData responsePacket);

    void broadcastPushResponsePacket(@NonNull String databaseID, @NonNull SQLCommandUpdateResponsePacketData responsePacket);

    DBQueryResult executeQuery(Connection connection, String raw);

    DBUpdateResult executeUpdate(Connection connection, String raw);

    DBCommandError generatePullError(UserData ud, DBQueryResult result, Map<String, Object> appendix);

    DBCommandError generatePushError(UserData ud, DBUpdateResult result, Map<String, Object> appendix);

    File getDatabaseFile(@NonNull String databaseID);

    long getDatabaseAbsoluteSizeInBytes(@NonNull String databaseID);
}
