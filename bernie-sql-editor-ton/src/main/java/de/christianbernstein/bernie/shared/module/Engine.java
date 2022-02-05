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

package de.christianbernstein.bernie.shared.module;

import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.document.IDocument;
import de.christianbernstein.bernie.shared.event.EventAPI;
import de.christianbernstein.bernie.shared.misc.BoolAccumulator;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.misc.Utils;
import lombok.Data;
import lombok.NonNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christian Bernstein
 */
@Data
public class Engine<T> implements IEngine<T> {

    private final EventAPI.IEventController<IEngine<T>> eventController = new EventAPI.DefaultEventController<>();

    private final List<ModularizedEntityHolder<T, ?>> entityHolders = new ArrayList<>();

    private final Supplier<Map<String, Document>> configurationLoader = HashMap::new;

    private final Function<String, Document> configFactory = s -> Document.empty();

    private final List<Module<T>> modules = new ArrayList<>();

    private final IDocument<?> sharedState = Document.empty();

    private final String id;

    private final T api;

    private ModuleLoadMode loadMode = ModuleLoadMode.STRICT;

    private Map<String, Document> moduleConfigurations;

    private boolean dependencyChecking = true;

    private String calibration = BASE_MODULE;

    public Engine(@NonNull final String id, final T api) {
        this.id = id;
        this.api = api;
    }

    public Engine(final T api) {
        this(UUID.randomUUID().toString(), api);
    }

    public Engine() {
        this(UUID.randomUUID().toString(), null);
    }

    @Override
    public T api() {
        return this.api;
    }

    @Override
    public IEngine<T> enableDependencyChecking() {
        this.setDependencyChecking(true);
        return this.updateCyclic(IEngine.MAX_CYCLIC_UPDATES);
    }

    @Override
    public IEngine<T> disableDependencyChecking() {
        this.setDependencyChecking(false);
        return this.updateCyclic(IEngine.MAX_CYCLIC_UPDATES);
    }

    @Override
    public IEngine<T> updateCyclic(int depth) {
        // todo originally (depth < 0) -> change it to 1, if bug, rollback to to 0
        if (depth < 1) {
            for (; ; ) {
                if (!this.updateWaitingModules()) {
                    break;
                }
            }
        }
        for (int i = 0; i < depth; i++) {
            if (!this.updateWaitingModules()) {
                break;
            }
        }
        return this;
    }

    public IEngine<T> setLoadMode(@NonNull final ModuleLoadMode loadMode) {
        this.loadMode = loadMode;
        return this;
    }

    @Override
    public IEngine<T> calibrate(String module) {
        this.getEventController().fire(new Events.ModuleCalibrationChangeEvent<>(this, this.calibration, module));
        this.calibration = module;
        return this;
    }

    @Override
    public IEngine<T> reset() {
        return this.calibrate(BASE_MODULE);
    }

    @SafeVarargs
    @Override
    public final List<Module<T>> get(@NonNull FilterMode mode, Predicate<Module<?>>... filters) {
        switch (mode) {
            case OR:
                final List<Module<T>> modules = new ArrayList<>();
                for (final Predicate<Module<?>> filter : filters) {
                    modules.addAll(this.getModules().stream().filter(filter).collect(Collectors.toList()));
                }
                return modules;
            case AND:
                Stream<Module<T>> stream = this.getModules().stream();
                for (final Predicate<Module<?>> filter : filters) {
                    stream = stream.filter(filter);
                }
                return stream.collect(Collectors.toUnmodifiableList());
            default:
                return Collections.emptyList();
        }
    }

    @SafeVarargs
    @Override
    public final List<Module<T>> get(Predicate<Module<?>>... filters) {
        return IEngine.super.get(filters);
    }

    /**
     * todo introduce class with method isTagPresent(String tag)
     */
    @Override
    public List<ILoadValidator<T>> getInternalLoadValidators() {
        final List<ILoadValidator<T>> internals = new ArrayList<>();
        if (this.isDependencyChecking()) {
            internals.add(ILoadValidator.createDependencyLoadValidator());
        }
        return internals;
    }

    @Override
    public IEngine<T> addModularizedEntityHolder(@NonNull ModularizedEntityHolder<T, ?> holder) {
        if (this.getEntityHolder(holder.getId()).isEmpty()) {
            this.entityHolders.add(holder);
        }
        return this;
    }

    @Override
    public <S> Optional<ModularizedEntityHolder<T, S>> getEntityHolder(@NonNull String id) {
        final Optional<ModularizedEntityHolder<T, ?>> optional = this.entityHolders.stream().filter(holder -> holder.getId().equals(id)).findFirst();
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked") final ModularizedEntityHolder<T, S> holder = (ModularizedEntityHolder<T, S>) optional.get();
            return Optional.of(holder);
        } catch (final ClassCastException e) {
            return Optional.empty();
        }
    }

    @Override
    public <S> IEngine<T> entityHolderAction(@NonNull String id, @NonNull Consumer<ModularizedEntityHolder<T, S>> handler) {
        this.<S>getEntityHolder(id).ifPresent(handler);
        return this;
    }

    @Override
    public List<Module<T>> getEngagedModules() {
        return this.get(IEngine.ENGAGED_MODULE_FILTER);
    }

    @Override
    public Optional<Module<T>> getModule(@NonNull String id) {
        return this.getEngagedModules().stream().filter(module -> module.getName().equals(id)).findFirst();
    }

    // todo switch to
    @Override
    public List<Module<T>> getStatorModules() {
        return this.get(IEngine.STATOR_MODULE_FILTER, IEngine.ENGAGED_MODULE_FILTER);
    }

    @Override
    public List<Module<T>> getWaitingModules() {
        return this.get(IEngine.ENGAGING_MODULE_FILTER);
    }

    // todo mark that the calibration must be set in order to work
    @Override
    public IEngine<T> addModuleEventHandler(EventAPI.Handler<IEngine<T>> handler) {
        handler.getMeta().put("module", this.getCalibration());
        this.getEventController().registerHandler(handler);
        return this;
    }

    // todo split into prepare / install
    @Override
    public final void deprecated_install(Module<?>... modules) {
        try {
            @SuppressWarnings("unchecked") final Module<T>[] refinedModules = (Module<T>[]) modules;
            for (final Module<T> module : refinedModules) {
                if (this.getModule(module.getName()).isPresent()) {
                    continue;
                }
                this.engage(module.getName());
            }
        } catch (final ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IEngine<T> register(boolean autoEngage, Module<?>... modules) {
        @SuppressWarnings("unchecked") final Module<T>[] cast = (Module<T>[]) modules;
        for (final Module<T> module : cast) {
            if (this.getModule(module.getName()).isPresent()) {
                continue;
            }
            this.modules.add(module);
            module.fireInternalContext(BasicModuleContextType.INSTALL.getBaseType(), this.getApi(), module, this);
            module.getInstaller().fire(this.getApi(), module, this);
            this.getEventController().fire(new Events.ModuleAddedEvent<>(this, module));
            module.setLifecycle(Lifecycle.INSTALLED);
            if (autoEngage) {
                this.engage(module.getName());
            }
        }
        return this;
    }

    @Override
    public boolean updateWaitingModules() {
        final Engine<T> engine = this;
        boolean changeOccurred = false;
        for (Module<T> module : engine.getWaitingModules()) {
            // Check loading condition
            final BoolAccumulator accumulator = BoolAccumulator.builder()
                    .conditions(module.getLoadValidators().stream().map(validator -> (Supplier<Boolean>) () -> validator.canLoad(engine, module)).collect(Collectors.toList()))
                    .switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                    .build();
            //noinspection StatementWithEmptyBody
            if (accumulator.get()) {
                // The module can now be engage
                this.engage(module.getName());
                changeOccurred = true;
            } else {
                // Not all validators passed, the module cannot be engaged
                // todo handle -> trigger event
            }
        }
        return changeOccurred;
    }

    // todo create method -> getEngage(able)Modules()
    @Override
    public IEngine<T> engage(@NonNull String moduleID) {
        this.get(FilterMode.OR, IEngine.ENGAGING_MODULE_FILTER, IEngine.INSTALLED_MODULE_FILTER).stream().filter(module -> module.getName().equals(moduleID)).findFirst().ifPresent(module -> {
            // todo use more generic approach
            if (module.getLifecycle() == Lifecycle.INSTALLED) {
                module.setLifecycle(Lifecycle.ENGAGING);
            }
            final Engine<T> engine = this;
            final List<ILoadValidator<T>> loadValidators = this.getInternalLoadValidators();
            loadValidators.addAll(module.getLoadValidators());
            final BoolAccumulator accumulator = BoolAccumulator.builder()
                    .conditions(loadValidators.stream().map(validator -> (Supplier<Boolean>) () -> validator.canLoad(engine, module)).collect(Collectors.toList()))
                    .switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                    .build();
            if (accumulator.get()) {
                // Module can be loaded
                final Duration loadTiming = Utils.durationMonitoredExecution(() -> {
                    module.fireInternalContext(BasicModuleContextType.ENGAGE.getBaseType(), this.getApi(), module, this);
                    module.getBootloader().fire(this.getApi(), module, this);
                    module.setLifecycle(Lifecycle.ENGAGED);
                });

                ConsoleLogger.def().log(
                        ConsoleLogger.LogType.INFO,
                        "Module Engine",
                        String.format("Module engaged '%s' in %sms", moduleID, loadTiming.toMillis())
                );

                this.updateCyclic(Engine.MAX_CYCLIC_UPDATES);
            } else {
                // Module cannot be loaded
                // todo handle
                // todo inc a load attempts variable
                // System.err.printf("Module cannot be engaged '%s'\n", moduleID);
            }
        });
        return this;
    }

    // todo implement
    // todo check if other modules have to be uninstalled
    // todo should a stator be able to disengage
    @Override
    public IEngine<T> disengage(@NonNull String moduleID) {
        this.getModule(moduleID).ifPresent(module -> {
            if (module.isStator()) {
                return;
            }
            module.fireInternalContext(BasicModuleContextType.DISENGAGE.getBaseType(), this.getApi(), module, this);
            module.getUninstaller().fire(this.getApi(), module, this);
            this.getEventController().fire(new Events.ModuleRemovedEvent<>(this, module));
            module.setLifecycle(Lifecycle.DISENGAGED);
        });
        return this;
    }

    // todo make deprecated
    // todo implement force option!
    // todo disengage
    @Override
    public void uninstall(String... names) {
        for (final String moduleName : names) {
            this.getModule(moduleName).ifPresent(module -> {
                if (module.isStator()) {
                    return;
                }
                module.getUninstaller().fire(this.getApi(), module, this);
                this.getEventController().fire(new Events.ModuleRemovedEvent<>(this, module));
                // Remove the module-specific event handlers
                this.getEventController().removeIf(handler -> {
                    final String location = handler.getMeta().getString("module");
                    if (location != null) {
                        return location.equals(moduleName);
                    }
                    return false;
                });
                // Remove all entities, related to this module
                this.getEntityHolders().forEach(entityHolder -> {
                    entityHolder.removeModule(moduleName);
                });
                module.setLifecycle(Lifecycle.INSTALLED);
            });
        }
    }

    @Override
    public void uninstallAll() {
        // this.uninstall(this.getModules().stream().map(Module::getName).toArray(String[]::new));
        this.getModules().stream().map(Module::getName).forEach(this::disengage);
    }

    @Override
    public void update(String... names) {
        final List<String> moduleNames = Arrays.asList(names);
        this.getEngagedModules().stream().filter(module -> moduleNames.contains(module.getName())).forEach(module -> {
            module.getUpdate().fire(this.api, module, this);
        });
    }

    @Override
    public void updateAll() {
        this.getEngagedModules().forEach(module -> {
            module.getUpdate().fire(this.api, module, this);
        });
    }

    @Override
    public void delete(boolean force, String... names) {
        for (final String name : names) {
            this.modules.forEach(module -> {
                if (module.getName().equals(name)) {
                    // Delete only if the module isn't a stator or force is available
                    if (!module.isStator() || force) {
                        // If the module is installed, uninstall it first
                        if (module.getLifecycle() == Lifecycle.ENGAGED) {
                            this.uninstall(name);
                        }
                        this.getModules().remove(module);
                    }
                }
            });
        }
    }

    @Override
    public void saveConfigurations() {
        // todo implement
    }

    @Override
    public Map<String, Document> getConfigurations() {
        return this.moduleConfigurations;
    }

    @Override
    public Document getConfigurationFor(String id) {
        return this.getConfigurations().entrySet().stream().filter(e -> e.getKey().equals(id)).map(Map.Entry::getValue).findFirst().orElseGet(() -> {
            // The configuration isn't available, create a new one
            return this.createConfiguration(id);
        });
    }

    @Override
    public Document createConfiguration(String id) {
        final Document configuration = this.getConfigFactory().apply(id);
        this.getConfigurations().putIfAbsent(id, configuration);
        this.saveConfigurations();
        return configuration;
    }
}
