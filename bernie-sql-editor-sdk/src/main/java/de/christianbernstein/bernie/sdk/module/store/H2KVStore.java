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

package de.christianbernstein.bernie.sdk.module.store;

import de.christianbernstein.bernie.sdk.db.DB;
import de.christianbernstein.bernie.sdk.misc.ObjectNotationLanguage;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class H2KVStore implements IKVStore {

    private final String storeName;

    private final ObjectNotationLanguage notationLanguage;

    private final DB.Update createTable, upsertKV, removeKV;

    private final DB.Query selectKV, selectKVs;

    private final Connection connection;

    private Instant lastLoadedTime;

    private boolean lazyLoading;

    @SuppressWarnings("all")
    public H2KVStore(@NonNull String storeName, @NonNull ObjectNotationLanguage notationLanguage, @NonNull Connection connection) {
        this.storeName = storeName;
        this.notationLanguage = notationLanguage;
        this.connection = connection;
        this.createTable = DB.Update.of("create table if not exists %s (key varchar(255) primary key, value text);", this.connection);
        this.upsertKV = DB.Update.of("merge into %s key (key) values('%s', '%s');", this.connection);
        this.removeKV = DB.Update.of("delete from %s where key = '%s'", this.connection);
        this.selectKV = DB.Query.of("select * from %s where key = '%s'", this.connection);
        this.selectKVs = DB.Query.of("select * from %s", this.connection);
    }

    @Override
    public IKVStore setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
        return this;
    }

    @Override
    public <T> T get(@NonNull String key) {
        String payload = this._select(key);
        if (payload == null) {
            return null;
        }
        if (this.getNotationLanguage().isClasslessTranslation()) {
            return this.getNotationLanguage().getSerialAdapter().deserialize(payload, null);
        } else {
            return null;
        }
    }

    @Override
    public <T> T get(@NonNull String key, @NonNull Class<T> type) {
        String payload = this._select(key);
        if (payload == null) {
            return null;
        }
        return this.getNotationLanguage().getSerialAdapter().deserialize(payload, type);
    }

    @Override
    public IKVStore set(@NonNull String key, @Nullable Object value) {
        assert value != null;
        final String serial = this.getNotationLanguage().getSerialAdapter().serialize(value, null);
        this.upsertKV.update(this.getStoreName(), serial);
        this.manifestCache();
        return this;
    }

    @Override
    public boolean has(@NotNull @NonNull String... keys) {
        return false;
    }

    @Override
    public IKVStore remove(@NotNull @NonNull String... key) {
        return null;
    }

    @Override
    public IKVStore set(@NonNull Map<String, Object> kvs) {
        return null;
    }

    @Override
    public Map<String, String> getCache() {
        return null;
    }

    @Override
    public int cacheUpdatesSinceCacheManifestation() {
        return 0;
    }

    @Override
    public boolean isCacheLive(boolean approximate) {
        return false;
    }

    @Override
    public IKVStore manifestCache() {
        return null;
    }

    @Override
    public IKVStore reset() {
        return null;
    }

    @Nullable
    private String _select(@NonNull String key) {
        if (this.isLazyLoading()) {
            return this.getCache().get(key);
        } else {
            try {
                return this.selectKV.query(this.getStoreName(), key).getString("value");
            } catch (final SQLException throwables) {
                throwables.printStackTrace();
                return null;
            }
        }
    }
}
