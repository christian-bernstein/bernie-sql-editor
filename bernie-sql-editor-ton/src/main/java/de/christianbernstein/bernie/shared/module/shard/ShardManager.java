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

package de.christianbernstein.bernie.shared.module.shard;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import de.christianbernstein.bernie.shared.module.Module;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
// todo introduce global installers
public class ShardManager<API> implements IShardManager<API> {

    @Getter
    private final Map<Class<?>, List<IShardInstaller<API>>> installers = new HashMap<>();

    @Getter
    private final List<IShardInstaller<API>> globalInstallers = new ArrayList<>();

    private final Map<Class<?>, Shard> shards = new ConcurrentHashMap<>();

    private final Module<API> module;

    public ShardManager(@NonNull final Module<API> module) {
        this.module = module;
        this.installDefaultInstantiateInstaller();
    }

    @Override
    public Shard get(@NonNull final Class<?> shardClass) {
        return this.shards.get(shardClass);
    }

    // todo make installation reversible, if error in process and module unable to be installed
    @Override
    public IShardManager<API> install(@NotNull final Class<?> shardClass) {
        if (this.shards.containsKey(shardClass)) {
            System.err.println("Module cannot be installed, it is already installed: " + shardClass.getName());
            return this;
        }
        final AtomicReference<Object> shardInstance = new AtomicReference<>();
        final AtomicBoolean canInstall = new AtomicBoolean(true);
        final Function<IShardInstaller<API>, Boolean> reactor = installer -> {
            final ShardInstallerResult result = installer.install(new ShardInstallContext().shard(shardClass), this.module, shardClass);
            if (!result.installable()) {
                canInstall.set(false);
                return false;
            }
            if (result.shard() != null && shardInstance.get() == null) {
                shardInstance.set(result.shard());
            }
            return true;
        };

        // Install via global installers
        for (final IShardInstaller<API> installer : this.globalInstallers) {
            if (!reactor.apply(installer)) {
                break;
            }
        }
        // Install via specified installers
        final List<IShardInstaller<API>> installersFor = this.getInstallersFor(shardClass);
        if (installersFor != null) {
            for (final IShardInstaller<API> installer : installersFor) {
                if (!reactor.apply(installer)) {
                    break;
                }
            }
        }
        if (canInstall.get()) {
            this.shards.putIfAbsent(shardClass, new Shard().shardClass(shardClass).shardInstance(shardInstance.get()));
        }
        return this;
    }

    @Override
    public List<IShardInstaller<API>> getInstallersFor(@NotNull final Class<?> shardClass) {
        return this.installers.get(shardClass);
    }

    @Override
    public IShardManager<API> addInstaller(@NonNull Class<?> shardClass, @NonNull IShardInstaller<API> installer) {
        if (this.installers.containsKey(shardClass)) {
            this.installers.get(shardClass).add(installer);
        } else {
            final List<IShardInstaller<API>> list = new ArrayList<>();
            list.add(installer);
            this.installers.put(shardClass, list);
        }
        return this;
    }

    @Override
    public IShardManager<API> addGlobalInstaller(@NonNull IShardInstaller<API> installer) {
        this.globalInstallers.add(installer);
        return this;
    }

    @Override
    public List<Shard> getAll() {
        return new ArrayList<>(this.shards.values());
    }

    @Override
    public List<Shard> getAllInstanceHoldingShards() {
        return this.getAll().stream().filter(shard -> shard.getInstance() != null).collect(Collectors.toList());
    }

    private void installDefaultInstantiateInstaller() {
        final BiConsumer<Class<?>, ShardInstallContext> constructor = (c, context) -> {
            Object o = null;
            try {
                final Constructor<?> cConstructor = c.getConstructor();
                cConstructor.setAccessible(true);
                o = cConstructor.newInstance();
            } catch (final InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (o != null) context.shard(o);
        };

        this.addGlobalInstaller((@NonNull ShardInstallContext context, @NonNull Module<API> module1, @NonNull Class<?> shardClass) -> {
             if (shardClass.isAnnotationPresent(UseGlobalConstruct.class)) {
                 final UseGlobalConstruct construct = shardClass.getAnnotation(UseGlobalConstruct.class);
                 if (construct.value()) {
                     constructor.accept(shardClass, context);
                 }
             } else {
                 constructor.accept(shardClass, context);
             }
             return ShardInstallerResult.merge(context);
        });
    }
}
