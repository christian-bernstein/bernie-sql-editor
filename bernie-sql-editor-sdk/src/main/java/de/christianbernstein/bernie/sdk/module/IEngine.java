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

package de.christianbernstein.bernie.sdk.module;

import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import de.christianbernstein.bernie.sdk.event.EventAPI;
import de.christianbernstein.bernie.sdk.misc.IConfigurationHolder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IEngine<T> extends EventAPI.IWithEventController<IEngine<T>>, IConfigurationHolder<Document> {

    int MAX_CYCLIC_UPDATES = 1024;

    String BASE_MODULE = "base_module";

    Predicate<Module<?>> ENGAGED_MODULE_FILTER = module -> module.getLifecycle() == Lifecycle.ENGAGED;

    Predicate<Module<?>> ENGAGING_MODULE_FILTER = module -> module.getLifecycle() == Lifecycle.ENGAGING;

    Predicate<Module<?>> INSTALLED_MODULE_FILTER = module -> module.getLifecycle() == Lifecycle.INSTALLED;

    Predicate<Module<?>> STATOR_MODULE_FILTER = Module::isStator;

    T api();

    String getId();

    ModuleLoadMode getLoadMode();

    IEngine<T> setLoadMode(@NonNull ModuleLoadMode loadMode);

    String getCalibration();

    IEngine<T> calibrate(String module);

    IEngine<T> reset();

    @SuppressWarnings("unchecked")
    List<Module<T>> get(@NonNull FilterMode mode, Predicate<Module<?>>... filters);

    default Module<T> getByName(@NonNull final String name) {
        @SuppressWarnings("unchecked")
        final List<Module<T>> modules = this.get(module -> module.getName().equals(name));
        return modules.size() > 0 ? modules.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    default List<Module<T>> getAll() {
        return this.get(FilterMode.AND);
    }

    @SuppressWarnings("unchecked")
    default List<Module<T>> get(Predicate<Module<?>>... filters) {
        return this.get(FilterMode.AND, filters);
    }

    List<ILoadValidator<T>> getInternalLoadValidators();

    List<ModularizedEntityHolder<T, ?>> getEntityHolders();

    IEngine<T> addModularizedEntityHolder(@NonNull ModularizedEntityHolder<T, ?> holder);

    <V> Optional<ModularizedEntityHolder<T, V>> getEntityHolder(@NonNull String id);

    // todo implement this method and feature
    // default <V> Optional<ModularizedEntityHolder<T, V>> holder(@NonNull String id) {
    // }

    <V> IEngine<T> entityHolderAction(@NonNull String id, @NonNull Consumer<ModularizedEntityHolder<T, V>> handler);

    List<Module<T>> getModules();

    /**
     * @return All currently engaged (active) modules
     */
    List<Module<T>> getEngagedModules();

    Optional<Module<T>> getModule(@NonNull String id);

    List<Module<T>> getStatorModules();

    // disengaged modules,
    List<Module<T>> getWaitingModules();

    IEngine<T> addModuleEventHandler(EventAPI.Handler<IEngine<T>> handler);

    boolean isDependencyChecking();

    IEngine<T> enableDependencyChecking();

    IEngine<T> disableDependencyChecking();

    IEngine<T> updateCyclic(int depth);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean updateWaitingModules();

    // todo why <?> -> <T>
    @Deprecated()
    void deprecated_install(Module<?>... modules);

    IEngine<T> register(boolean autoEngage, Module<?>... modules);

    default IEngine<T> register(Module<?>... modules) {
        return this.register(true, modules);
    }

    IEngine<T> engage(@NonNull String moduleID);

    IEngine<T> disengage(@NonNull String moduleID);

    void uninstall(String... names);

    void uninstallAll();

    void update(String... names);

    void updateAll();

    /**
     * Uninstalls the selected modules and deletes them from the engine's module list.
     * Stator modules must be removed with force -> param force be true
     *
     * @param force Overwrites a module's stator ability (By default, stator modules cannot be disengaged or uninstalled)
     * @param names List of identifiers from modules you want to delete
     */
    void delete(boolean force, String... names);

    IDocument<?> getSharedState();

}
