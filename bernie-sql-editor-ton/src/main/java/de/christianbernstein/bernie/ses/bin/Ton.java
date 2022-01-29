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

package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.ses.session.Session;
import de.christianbernstein.bernie.ses.user.IUser;
import de.christianbernstein.bernie.shared.event.EventAPI;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.misc.Utils;
import de.christianbernstein.bernie.shared.module.Engine;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.shared.union.DefaultEventManager;
import de.christianbernstein.bernie.shared.union.IEventManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@Getter
@Accessors(fluent = true)
public class Ton implements ITon {

    private IEngine<ITon> engine;

    private JavaReflectiveAnnotationAPI.JRA jra;

    private TonState tonState;

    private TonConfiguration configuration;

    private IEventManager eventManager;

    @Override
    public ITon start(@NonNull final TonConfiguration configuration) {
        final long s = Utils.durationMonitoredExecution(() -> {
            this.configuration = configuration;
            this.tonState = TonState.LAUNCHING;
            this.engine = new Engine<ITon>(configuration.getTonEngineID(), this).enableDependencyChecking();
            this.eventManager = new DefaultEventManager();
            this.initJRA();
            this.tonState = TonState.ONLINE;
        }).toSeconds();

        ConsoleLogger.def().log(
                ConsoleLogger.LogType.INFO,
                "central module",
                "Ton server online, it took " + String.format("%dh %02dm %02ds", s / 3600, (s % 3600) / 60, (s % 60))
        );
        return this;
    }

    @Override
    public ITon shutdown() {
        this.tonState = TonState.STOPPING;
        this.engine().uninstallAll();
        this.tonState = TonState.PREPARED;
        return this;
    }

    @Override
    public <ModuleClass> ModuleClass require(@NonNull Class<ModuleClass> baseClass, @NonNull String module) {
        return this.engine()
                .getModule(module)
                .orElseThrow()
                .getShardManager()
                .get(baseClass)
                .getInstance();
    }

    @Override
    public <T, ID extends Serializable> Centralized<H2Repository<T, ID>> db(@NonNull Class<T> type) {
        return Centralized.constify(new H2Repository<>(type, this.configuration().getInternalDatabaseConfiguration()));
    }

    @Override
    public IUser getUserFromSessionID(@NonNull UUID sessionID) {
        final Session session = this.sessionModule().getOrCreateSession(sessionID);
        return this.userModule().getUser(session.getCredentials().getUsername());
    }

    @Override
    public @NonNull ITon me() {
        return this;
    }

    private void initJRA() {
        this.jra = JavaReflectiveAnnotationAPI.JRA.builder()
                .path(Constants.rootPackage)
                .classSupplier(JavaReflectiveAnnotationAPI.Defaults.orgReflectionsClassSupplier)
                .build();
        // Load and cache the classes from the classpath
        jra.init();
        // Execute the phases in given order
        final Document meta = Document.of("ton", this);
        Arrays.asList(this.configuration.getJraPhaseOrder()).forEach(phases -> {
            jra.process(meta, phases);
        });
    }
}
