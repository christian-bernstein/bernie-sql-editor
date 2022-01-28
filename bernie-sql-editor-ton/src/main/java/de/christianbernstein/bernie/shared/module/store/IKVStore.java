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

package de.christianbernstein.bernie.shared.module.store;

import de.christianbernstein.bernie.shared.misc.ObjectNotationLanguage;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("UnusedReturnValue")
public interface IKVStore {

    String getStoreName();

    ObjectNotationLanguage getNotationLanguage();

    <T> T get(@NonNull String key);

    <T> T get(@NonNull String key, @NonNull Class<T> type);

    IKVStore set(@NonNull String key, @Nullable Object value);

    boolean has(@NonNull String... keys);

    IKVStore remove(@NonNull String... key);

    IKVStore set(@NonNull Map<String, Object> kvs);

    Map<String, String> getCache();

    int cacheUpdatesSinceCacheManifestation();

    boolean isCacheLive(boolean approximate);

    IKVStore manifestCache();

    IKVStore reset();

    IKVStore setLazyLoading(boolean lazy);

    boolean isLazyLoading();

    @Nullable
    Instant getLastLoadedTime();
}
