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
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.module.Dependency;
import de.christianbernstein.bernie.shared.module.IBaseModuleClass;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import de.christianbernstein.bernie.shared.module.Module;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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

    Map<String, List<DBListenerID>> sqlCommandStreamConnectionLookup();

    boolean unloadDatabase(String dbID);

    List<IDatabaseAccessPoint> activeDatabases();

    String copyDatabase(String dbID);

    Configuration getRootConfiguration();

    SessionFactory getRootSessionFactory();



}
