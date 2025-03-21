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
import ch.qos.logback.classic.Logger;
import de.christianbernstein.bernie.modules.net.NetModuleConfigShard;
import de.christianbernstein.bernie.modules.session.Session;
import de.christianbernstein.bernie.modules.user.IUser;
import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.misc.ConsoleColors;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import de.christianbernstein.bernie.sdk.misc.Resource;
import de.christianbernstein.bernie.sdk.misc.Utils;
import de.christianbernstein.bernie.sdk.module.Engine;
import de.christianbernstein.bernie.sdk.module.IEngine;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.sdk.union.DefaultEventManager;
import de.christianbernstein.bernie.sdk.union.IEventManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * todo ensure if debugConfiguration isn't used anywhere, else than in configuration()
 *
 * @author Christian Bernstein
 */
@Getter
@Accessors(fluent = true)
public class Ton implements ITon {

    private static final List<TonBootProfile> bootProfiles = new ArrayList<>();

    static {
        Ton.bootProfiles.add(TonBootProfile.builder().name("c").name("conf").name("config").name("configuration").mainSupplier(ConfiguratorMain::new).build());
    }

    private final Map<String, List<CountDownLatch>> syncLatches = new ConcurrentHashMap<>();

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

    private int nextPoolID;

    @Accessors()
    private boolean preflight;

    public Ton() {
        this.arguments = Document.empty();
    }

    public Ton(@Nullable final Document arguments) {
        this.arguments = Objects.requireNonNullElse(arguments, Document.empty());
    }

    @Override
    public void installGlobalStringReplacers() {
        Map<String, Supplier<String>> map = this.globalStringReplacers;
        if (map == null) {
            map = this.globalStringReplacers = new HashMap<>();
        } else if (!map.isEmpty()) {
            map.clear();
        }
        final TonConfiguration configuration = configuration();
        map.put("root_dir", configuration::getRootDir);
        map.put("blue", () -> ConsoleColors.PURPLE);
        map.put("reset", () -> ConsoleColors.RESET);
        map.put("date_year", () -> String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        map.put("config_dir", () -> this.interpolate(configuration.getConfigPath()));
        map.put("config_file_ext", configuration::getDefaultConfigFileExtension);

        if (!this.isPreflight()) {
            map.put("ssl_dir", () -> this.config(NetModuleConfigShard.class, "net_config", NetModuleConfigShard.builder().build()).load().getSslCertificateDir());
        }
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
        this.defaultConfiguration = configuration;
        this.updateLoggers();
        this.arguments().ifPresentOr("exec", (String procedure) -> {
            this.preflight = true;
            this.startInPreflightMode(configuration, procedure);
        }, doc -> {
            // Starting ton in the default manner.
            this.preflight = false;
            this.startInDefaultMode(configuration, autoConfigReload);
        });
        return this;
    }

    private void updateLoggers() {
        this.configuration().getLoggerConfigs().forEach(config -> {
            Logger logger = null;
            switch (config.getType()) {
                case NAME -> logger = (Logger) LoggerFactory.getLogger(config.getLogger());
                case CLASS -> {
                    try {
                        final Class<?> loggerClass = Class.forName(config.getLogger());
                        logger = (Logger) LoggerFactory.getLogger(loggerClass);
                    } catch (final ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (logger != null) {
                logger.setLevel(Level.toLevel(config.getLevel()));
            } else {
                ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "startup", String.format("Cannot generate Logger instance for logger '%s' in %s-mode", config.getLogger(), config.getType().name().toLowerCase()));
            }
        });
    }

    /**
     * Executing a preflight method. Calling preflight mode using the 'exec'-argument as a program argument
     * Example: java -jar ton.jar -exec=a_preflight_script
     *
     * @param procedure The selected procedure to execute
     */
    private void startInPreflightMode(@NonNull TonConfiguration configuration, @NonNull String procedure) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "preflight", String.format("Executing Ton in preflight mode (%s)", procedure));
        Ton.bootProfiles.stream().filter(profile -> profile.getNames().contains(procedure)).findAny().ifPresentOrElse(profile -> {
            try {
                final AtomicReference<MainResult> resultRef = new AtomicReference<>();
                final long s = Utils.durationMonitoredExecution(() -> resultRef.set(profile.getMainSupplier().get().main(this))).toSeconds();
                final MainResult result = resultRef.get();
                final String processingTimeString = String.format("%dh %02dm %02ds", s / 3600, (s % 3600) / 60, (s % 60));
                if (result.getExitCode() == 0) {
                    ConsoleLogger.def().log(ConsoleLogger.LogType.SUCCESS, "preflight", String.format(
                            "Preflight execution was%s successful%s and finished after %s with exit code %s",
                            ConsoleColors.GREEN_BOLD_BRIGHT,
                            ConsoleColors.RESET,
                            processingTimeString,
                            result.getExitCode()));
                } else {
                    ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "preflight", String.format(
                            "Preflight execution was%s unsuccessful%s and finished after %s with exit code %s",
                            ConsoleColors.RED_BOLD_BRIGHT,
                            ConsoleColors.RESET,
                            processingTimeString,
                            result.getExitCode()));
                }
            } catch (final Exception e) {
                ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "preflight", String.format("An unhandled error occurred while executing main method for procedure '%s'", procedure));
                e.printStackTrace();
            }
        }, () -> {
            ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "preflight", String.format(
                    "No preflight registered with name of '%s'. Available preflight modes are '%s'", procedure,
                    Ton.bootProfiles.stream().map(TonBootProfile::getNames).map(Object::toString).collect(Collectors.joining(", "))));
        });

        this.fireSyncEvent(TonSyncLatch.CLOSE.getName());
    }

    private void onMainConfigFileReload(@NonNull TonConfiguration config) {
        this.updateLoggers();
    }

    private void startInDefaultMode(@NonNull TonConfiguration configuration, boolean autoConfigReload) {
        if (this.tonState != null && this.tonState != TonState.PREPARED) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.WARN, "central module", String.format("Cannot start ton in default mode, because the state is not 'prepared' or null (state: %s)", this.tonState));
            return;
        }

        // ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "preflight", "Executing Ton in main mode");
        final long s = Utils.durationMonitoredExecution(() -> {
            this.nextPoolID = 0;
            this.pools = new HashMap<>();
            this.schedulingPools = new HashMap<>();
            this.configResourcePath = "ton/ton_config.yaml";
            this.tonState = TonState.LAUNCHING;
            this.configResource = new Resource<TonConfiguration>(this.configResourcePath()).init(true, () -> configuration).doIf(autoConfigReload, Resource::enableAutoFileUpdate).registerOnLoad(this::onMainConfigFileReload);
            this.globalStringReplacers = new HashMap<>();
            this.installGlobalStringReplacers();
            this.updateLoggers();
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

        this.fireSyncEvent(TonSyncLatch.OPEN.getName());
    }

    @Override
    public ITon start(@NonNull final TonConfiguration configuration) {
        return this.start(configuration, true);
    }

    @Override
    public ITon shutdown() {
        return this.shutdown(true);
    }

    @Override
    public ITon shutdown(boolean releaseSyncLatches) {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "central module", "Shutting down…");
        if (this.tonState != TonState.ONLINE) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.WARN, "central module", String.format("Cannot shutdown ton, because the state is not online (state: %s)", this.tonState));
            return this;
        }

        this.tonState = TonState.STOPPING;
        this.engine().uninstallAll();
        this.pools.forEach((id, service) -> {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "central module", String.format("Shutdown of executor pool '%s'", id));
            try {
                service.shutdownNow();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        this.schedulingPools.forEach((id, service) -> service.shutdownNow());
        this.configResource.stop();
        this.pools.clear();
        this.schedulingPools.clear();
        this.tonState = TonState.PREPARED;

        if (releaseSyncLatches) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.SUCCESS, "central module", "Shutdown successfully completed, releasing close sync latches…");
            this.fireSyncEvent(TonSyncLatch.CLOSE.getName());
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.SUCCESS, "central module", "Shutdown successfully completed");
        }

        return this;
    }

    @Override
    public ExecutorService pool(String pool) {
        return this.pool(pool, null);
    }

    private int generateNextExecutorPoolID() {
        final int npID = this.nextPoolID;
        this.nextPoolID++;
        return npID;
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
                service = Executors.newSingleThreadExecutor(r -> new Thread(r, String.format(
                        "hercules-pool-%s-%s", this.generateNextExecutorPoolID(), pool
                )));
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

    /**
     * todo Test method speed
     * todo make syntax / quote a static, non editable var -> faster interpolation
     */
    @Override
    public String interpolate(String format) {
        final String syntax = this.configuration().getVariableInterpolationSyntax();
        final String quote = Pattern.quote(syntax);
        final AtomicReference<String> form = new AtomicReference<>(format);

        // -> new
        final AtomicBoolean hasInterpolated = new AtomicBoolean(false);

        this.globalStringReplacers.forEach((key, stringSupplier) -> {
            form.getAndUpdate(f -> {
                try {
                    final String richKey = String.format(quote, key);
                    if (f.contains(String.format(syntax, key))) {

                        // -> new
                        hasInterpolated.set(true);

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


        // -> new
        if (hasInterpolated.get()) {
            return this.interpolate(form.get());
        } else {
            return form.get();
        }


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
            if (this.configResource() == null) {
                return this.defaultConfiguration();
            } else {
                final TonConfiguration configuration = this.configResource().use(false);
                if (configuration == null) {
                    return this.defaultConfiguration();
                } else {
                    return configuration;
                }
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
            // ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "JRA", String.format("Processing phase pod '%s'", Arrays.toString(phases)));
            final Duration duration = Utils.durationMonitoredExecution(() -> jra.process(meta, phases));
            // ConsoleLogger.def().log(ConsoleLogger.LogType.DEBUG, "JRA", String.format("Phase pod execution '%s' took %ss", Arrays.toString(phases), duration.toSeconds()));
        });
    }

    private void fireSyncEvent(@NonNull final String latchSyncEvent) {
        if (this.syncLatches.containsKey(latchSyncEvent)) {
            this.syncLatches.get(latchSyncEvent).forEach(latch -> {
                try {
                    latch.countDown();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void sync(@NonNull final String latchSyncEvent, @IntRange(from = 1) final int latchSyncAmount) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(latchSyncAmount);
        if (!this.syncLatches.containsKey(latchSyncEvent)) {
            this.syncLatches.put(latchSyncEvent, List.of(latch));
        } else {
            this.syncLatches.get(latchSyncEvent).add(latch);
        }
        latch.await();
    }

    @Override
    public void syncClose() throws InterruptedException {
        this.sync(TonSyncLatch.CLOSE.getName(), 1);
    }

    @Override
    public void syncOpen() throws InterruptedException {
        this.sync(TonSyncLatch.OPEN.getName(), 1);
    }
}
