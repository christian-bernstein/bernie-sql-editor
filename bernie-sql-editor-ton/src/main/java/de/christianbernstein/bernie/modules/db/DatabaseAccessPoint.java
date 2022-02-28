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

import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import lombok.Data;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
@Data
public class DatabaseAccessPoint implements IDatabaseAccessPoint {

    private final String dbID;

    @Getter
    private SessionFactory sessionFactory;

    @Getter
    private final Configuration configuration;

    @Override
    public String getID() {
        return this.dbID;
    }

    @Override
    public IDatabaseAccessPoint session(Consumer<Session> action) {
        if (this.sessionFactory == null) {
            try {
                this.sessionFactory = configuration.buildSessionFactory();
            } catch (final Exception e) {
                e.printStackTrace();
                if (this.sessionFactory != null) {
                    this.sessionFactory.close();
                    this.sessionFactory = null;
                }
            }
        }
        if (this.sessionFactory != null) {
            Transaction transaction = null;
            try (final Session session = this.sessionFactory.openSession()) {
                transaction = session.beginTransaction();
                action.accept(session);
                session.flush();
                transaction.commit();
            } catch (final Exception e) {
                ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "DBAccessPoint", String.format(
                        "Error while processing sql command. Message: '%s', Type: '%s'",
                        e.getMessage(),
                        e.getClass().getName()
                ));
                e.printStackTrace();
                // e.printStackTrace();
                if (transaction != null) {
                    // todo reactivate feature
                    // transaction.rollback();
                }
            }
        }
        return this;
    }

    @Override
    public void unload() {

    }
}
