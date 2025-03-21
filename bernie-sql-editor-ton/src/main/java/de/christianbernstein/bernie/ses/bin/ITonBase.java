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

import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.misc.Resource;
import de.christianbernstein.bernie.sdk.module.IEngine;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.sdk.union.IEventManager;
import lombok.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("UnusedReturnValue")
public interface ITonBase<Impl extends ITon> {

    Impl start(@NonNull TonConfiguration configuration);

    Impl start(@NonNull TonConfiguration configuration, boolean autoConfigReload);

    Impl shutdown();

    Impl shutdown(boolean releaseSyncLatches);

    boolean isPreflight();

    ExecutorService pool(String pool);

    ExecutorService pool(String pool, @Nullable Supplier<ExecutorService> factory);

    ScheduledExecutorService schedulingPool(String pool);

    ScheduledExecutorService schedulingPool(String pool, @Nullable Supplier<ScheduledExecutorService> factory);

    String interpolate(String format);

    <Config> Resource<Config> config(Class<Config> type, String id, @Nullable Config def);

    IEventManager eventManager();

    Resource<TonConfiguration> configResource();

    TonConfiguration configuration();

    TonConfiguration defaultConfiguration();

    TonState tonState();

    IEngine<Impl> engine();

    JavaReflectiveAnnotationAPI.JRA jra();

    <ModuleClass> ModuleClass require(@NonNull Class<ModuleClass> baseClass, @NonNull String module);

    <T, ID extends Serializable> Centralized<H2Repository<T, ID>> db(@NonNull Class<T> type);

    /**
     * main arguments will be passed to this variable
     */
    IDocument<Document> arguments();

    Impl ifInMode(TonMode mode, Runnable action);

    Impl ifDebug(Runnable action);

    void installGlobalStringReplacers();

    void sync(@NonNull final String latchSyncEvent, @IntRange(from = 1) final int latchSyncAmount) throws InterruptedException;

    void syncClose() throws InterruptedException;

    void syncOpen() throws InterruptedException;
}
