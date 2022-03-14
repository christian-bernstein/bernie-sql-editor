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

import ch.qos.logback.classic.Level;
import de.christianbernstein.bernie.shared.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is the bootstrap class for the embedded ton server.
 *
 * @author Christian Bernstein
 */
public class TonLauncher {

    @SuppressWarnings("all")
    private static Optional<ITon> ton = Optional.empty();

    public static void main(String[] args) {
        // LoggerFactory.getLogger(TonLauncher.class).error("Hell world");

        ch.qos.logback.classic.Logger root;

        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.reflections");
        root.setLevel(Level.OFF);

        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.hibernate");
        root.setLevel(Level.OFF);

        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.jboss");
        root.setLevel(Level.OFF);

        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.jboss");
        root.setLevel(Level.OFF);

        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.java_websocket");
        root.setLevel(Level.OFF);

        // BernieSystemPrintAdapter.systemInstall();

        new Ton(Document.fromArgumentsArray(args)).$(iTon -> TonLauncher.ton = Optional.of(iTon)).start(TonConfiguration.builder()
                .internalDatabaseConfiguration(H2RepositoryConfiguration.builder().hbm2DDLMode(HBM2DDLMode.UPDATE).databaseDir("./db/").database("ton").username("root").password("root").build())
                .mode(TonMode.DEBUG)
                .workingDirectory("./ton/")
                .build()
        );
    }
}
