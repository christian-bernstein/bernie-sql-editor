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

import de.christianbernstein.bernie.modules.session.Session;
import de.christianbernstein.bernie.modules.user.IUser;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.db.H2Repository;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.misc.Resource;
import de.christianbernstein.bernie.shared.misc.Utils;
import de.christianbernstein.bernie.shared.module.Engine;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.shared.union.DefaultEventManager;
import de.christianbernstein.bernie.shared.union.IEventManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * todo ensure if debugConfiguration isn't used anywhere, else than in configuration()
 *
 * @author Christian Bernstein
 */
@Getter
@Accessors(fluent = true)
public class Ton implements ITon {

    private final Document arguments;

    private IEngine<ITon> engine;

    private JavaReflectiveAnnotationAPI.JRA jra;

    private TonState tonState;

    private TonConfiguration defaultConfiguration;

    private IEventManager eventManager;

    private String configResourcePath;

    private Resource<TonConfiguration> configResource;

    private Map<String, Supplier<String>> globalStringReplacers;

    private Map<String, ExecutorService> pools;

    private Map<String, ScheduledExecutorService> schedulingPools;

    public Ton() {
        this.arguments = Document.empty();
    }

    public Ton(@Nullable final Document arguments) {
        this.arguments = Objects.requireNonNullElse(arguments, Document.empty());
    }

    private void initGlobalStringReplacers() {
        final Map<String, Supplier<String>> map = this.globalStringReplacers;
        final TonConfiguration configuration = configuration();
        map.put("root_dir", configuration::getRootDir);
        // todo get colors right c.c
        map.put("blue", () -> ConsoleColors.PURPLE);
        map.put("reset", () -> ConsoleColors.RESET);
        map.put("date_year", () -> String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        map.put("config_dir", () -> this.interpolate(configuration.getConfigPath()));
    }

    private void initBootingScreen() {
        if (this.configuration().isEnableBanner()) {
            final String rawBanner = String.join("\n", this.configuration().getBanner());
            final String banner = this.interpolate(rawBanner);
            System.out.println(ConsoleColors.confined(banner));
        }
    }

    @Override
    public ITon start(@NonNull TonConfiguration configuration, boolean autoConfigReload) {
        final long s = Utils.durationMonitoredExecution(() -> {
            this.pools = new HashMap<>();
            this.schedulingPools = new HashMap<>();
            this.configResourcePath = "ton/ton_config.yaml";
            this.defaultConfiguration = configuration;
            this.tonState = TonState.LAUNCHING;
            this.configResource = new Resource<TonConfiguration>(this.configResourcePath()).init(true, () -> configuration).doIf(autoConfigReload, Resource::enableAutoFileUpdate);
            this.globalStringReplacers = new HashMap<>();
            this.initGlobalStringReplacers();
            this.initBootingScreen();
            this.pool("main");
            this.engine = new Engine<ITon>(configuration.getTonEngineID(), this).enableDependencyChecking();
            this.eventManager = new DefaultEventManager();
            this.initJRA();
            this.tonState = TonState.ONLINE;
        }).toSeconds();

        ConsoleLogger.def().log(
                ConsoleLogger.LogType.SUCCESS,
                "central module",
                "Ton server online, it took " + String.format("%dh %02dm %02ds", s / 3600, (s % 3600) / 60, (s % 60))
        );
        return this;
    }

    @Override
    public ITon start(@NonNull final TonConfiguration configuration) {
        return this.start(configuration, true);
    }

    @Override
    public ITon shutdown() {
        this.tonState = TonState.STOPPING;
        this.engine().uninstallAll();

        this.pools.forEach((id, service) -> service.shutdownNow());
        this.schedulingPools.forEach((id, service) -> service.shutdownNow());

        this.tonState = TonState.PREPARED;
        return this;
    }

    @Override
    public ExecutorService pool(String pool) {
        return this.pool(pool, Executors::newSingleThreadExecutor);
    }

    @Override
    public ExecutorService pool(String pool, @Nullable Supplier<ExecutorService> factory) {
        if (this.pools.containsKey(pool)) {
            return this.pools.get(pool);
        } else {
            ExecutorService service = null;
            if (factory != null) {
                try {
                    service = factory.get();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            if (service == null) {
                service = Executors.newSingleThreadExecutor();
            }

            ConsoleLogger.def().log(
                    ConsoleLogger.LogType.INFO,
                    "central module",
                    String.format("Created new executor pool '%s'", pool)
            );

            this.pools.put(pool, service);
            return service;
        }
    }

    @Override
    public ScheduledExecutorService schedulingPool(String pool) {
        return this.schedulingPool(pool, Executors::newSingleThreadScheduledExecutor);
    }

    @Override
    public ScheduledExecutorService schedulingPool(String pool, @Nullable Supplier<ScheduledExecutorService> factory) {
        if (this.schedulingPools.containsKey(pool)) {
            return this.schedulingPools.get(pool);
        } else {
            ScheduledExecutorService service = null;
            if (factory != null) {
                try {
                    service = factory.get();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            if (service == null) {
                service = Executors.newSingleThreadScheduledExecutor();
            }
            this.schedulingPools.put(pool, service);
            return service;
        }
    }

    @Override
    public String interpolate(String format) {
        final String syntax = this.configuration().getVariableInterpolationSyntax();
        final String quote = Pattern.quote(syntax);
        final AtomicReference<String> form = new AtomicReference<>(format);
        this.globalStringReplacers.forEach((key, stringSupplier) -> {
            form.getAndUpdate(f -> {
                try {
                    final String richKey = String.format(quote, key);
                    if (f.contains(String.format(syntax, key))) {
                        return f.replaceAll(richKey, stringSupplier.get());
                    } else {
                        // Not checking this will result in continuous loop, when calling interpolate() inside an interpolation
                        return f;
                    }
                } catch (final StackOverflowError e) {
                    new IllegalStateException(
                            String.format("StackOverflowError caused. This might indicate a string interpolation which interpolates itself continuously. [interpolator key: %s, interpolation target: %s]", key, f), e
                    ).printStackTrace();
                    return f;
                }
            });
        });
        return form.get();
    }

    @Override
    public <Config> Resource<Config> config(Class<Config> type, String id, @Nullable Config def) {
        final TonConfiguration configuration = this.configuration();
        final String configPath = this.interpolate(configuration.getModuleConfigPath());
        final String fileExt = configuration.getDefaultConfigFileExtension();
        return new Resource<Config>(String.format("%s%s.%s", configPath, id, fileExt)).init(true, () -> def);
    }

    @Override
    public TonConfiguration configuration() {
        try {
            final TonConfiguration configuration = this.configResource().use(false);
            if (configuration == null) {
                return this.defaultConfiguration();
            } else {
                return configuration;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return this.defaultConfiguration();
        }
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

    /**
     * todo save variable to local map
     * Provides access to internal databases.
     */
    @Override
    public <T, ID extends Serializable> Centralized<H2Repository<T, ID>> db(@NonNull Class<T> type) {
        return Centralized.constify(new H2Repository<>(type, this.configuration().getInternalDatabaseConfiguration()));
    }

    @Override
    public ITon ifInMode(TonMode mode, Runnable action) {
        if (this.configuration().getMode() == mode) {
            action.run();
        }
        return this;
    }

    @Override
    public ITon ifDebug(Runnable action) {
        return this.ifInMode(TonMode.DEBUG, action);
    }

    @Override
    public IUser getUserFromSessionID(@NonNull UUID sessionID) {
        final Session session = this.sessionModule().getOrCreateSession(sessionID);
        return this.userModule().getUserOfUsername(session.getCredentials().getUsername());
    }

    @Override
    public @NonNull ITon me() {
        return this;
    }

    private void initJRA() {
        this.jra = JavaReflectiveAnnotationAPI.JRA.builder().path(Constants.rootPackage).classSupplier(JavaReflectiveAnnotationAPI.Defaults.orgReflectionsClassSupplier).build();
        // Load and cache the classes from the classpath
        jra.init();
        // Execute the phases in given order
        final Document meta = Document.of("ton", this);
        Arrays.asList(this.defaultConfiguration.getJraPhaseOrder()).forEach(phases -> {
            jra.process(meta, phases);
        });
    }
}
