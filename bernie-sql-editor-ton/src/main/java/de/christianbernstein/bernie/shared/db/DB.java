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

package de.christianbernstein.bernie.shared.db;

import de.christianbernstein.bernie.shared.misc.IBindable;
import lombok.Data;
import lombok.NonNull;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DB {

    @Data
    public static class Procedure implements IBindable.Default<Procedure, Connection> {

        private Connection connection;

        public Procedure(@Language("H2") @NonNull final String sql) {
            this.sql = sql;
        }

        @Language("H2")
        private final String sql;

        @Override
        public @NonNull Procedure bind(@NonNull Connection object) {
            this.connection = object;
            return this;
        }

        @Override
        public @NonNull Procedure unbind() {
            this.connection = null;
            return this;
        }

        @Override
        public Connection getBoundedObject() {
            return this.connection;
        }

        @Override
        public  @NonNull Procedure __getInstance() {
            return this;
        }

        public void onError(@NotNull @NonNull Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @NonNull
    public static String enrich(@Language("H2") @NonNull final String rawSql, final String... parameters) {
        return String.format(rawSql, (Object[]) parameters);
    }

    public static final class Update extends Procedure {

        public Update(@Language("H2") @NonNull String sql) {
            super(sql);
        }

        public int update(@Nullable Connection connection, String... parameters) {
            final AtomicInteger rCode = new AtomicInteger();
            this.executePrioritizedWith(connection, connectionIBindable -> {
                final Connection finalConnection = connectionIBindable.getBoundedObject();
                try {
                    PreparedStatement statement = finalConnection.prepareStatement(DB.enrich(this.getSql(), parameters));
                    rCode.set(statement.executeUpdate());
                } catch (final SQLException throwable) {
                    throwable.printStackTrace();
                }
            });
            return rCode.get();
        }

        public int update(String... parameters) {
            return this.update(null, parameters);
        }

        public int update() {
            return this.update((String) null);
        }

        @NotNull
        @Contract("_ -> new")
        public static Update of(@NonNull @Language("H2") final String sql) {
            return new Update(sql);
        }

        @NotNull
        public static Update of(@NonNull @Language("H2") final String sql, @NonNull final Connection connection) {
            final Update update = new Update(sql);
            update.bind(connection);
            return update;
        }
    }

    public static final class Query extends Procedure {

        public Query(@Language("H2") @NonNull String sql) {
            super(sql);
        }

        public ResultSet query(@Nullable Connection connection, String... parameters) {
            final AtomicReference<ResultSet> rSet = new AtomicReference<>();
            this.executePrioritizedWith(connection, connectionIBindable -> {
                final Connection finalConnection = connectionIBindable.getBoundedObject();
                try {
                    PreparedStatement statement = finalConnection.prepareStatement(DB.enrich(this.getSql(), parameters));
                    rSet.set(statement.executeQuery());
                } catch (final SQLException throwable) {
                    throwable.printStackTrace();
                }
            });
            return rSet.get();
        }

        public ResultSet query(String... parameters) {
            return this.query(null, parameters);
        }

        public ResultSet query() {
            return this.query((String) null);
        }

        @NotNull
        @Contract("_ -> new")
        public static Query of(@NonNull @Language("H2") final String sql) {
            return new Query(sql);
        }

        @NotNull
        public static Query of(@NonNull @Language("H2") final String sql, @NonNull final Connection connection) {
            final Query query = new Query(sql);
            query.bind(connection);
            return query;
        }
    }

    public enum Method {

        UPDATE, QUERY, UNDEFINED

    }
}

