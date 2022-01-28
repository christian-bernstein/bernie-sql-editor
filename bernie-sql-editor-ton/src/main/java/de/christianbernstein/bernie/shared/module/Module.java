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
import de.christianbernstein.bernie.shared.misc.IFluently;
import de.christianbernstein.bernie.shared.misc.ILambdaNamespace;
import de.christianbernstein.bernie.shared.misc.LambdaNamespace;
import de.christianbernstein.bernie.shared.module.shard.IShardManager;
import de.christianbernstein.bernie.shared.module.shard.Shard;
import de.christianbernstein.bernie.shared.module.shard.ShardManager;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class Module<T> implements IFluently<Module<T>> {

    @Deprecated
    @Getter
    @Builder.Default
    @Accessors(fluent = true)
    private final ILambdaNamespace<IDocument<?>> exposedAPI = new LambdaNamespace<>();

    @NonNull
    private final String name;

    @NonNull
    @Singular
    private final List<Dependency> dependencies;

    @Builder.Default
    private final IDocument<?> state = Document.empty();

    @Builder.Default
    private final boolean stator = false;

    @Singular
    private final Set<ILoadValidator<T>> loadValidators;

    private Lifecycle lifecycle = Lifecycle.INSTALLED;

    // todo implement
    @Builder.Default
    private final boolean deprecated = false;

    // todo implement
    private final IShardManager<T> shardManager = new ShardManager<>(this);

    private final Map<String, List<IModuleContext<T>>> internalContexts = new HashMap<>();

    @NonNull
    @Builder.Default
    private final IModuleContext<T> installer = (T api, Module<T> module, IEngine<T> manager) -> {
    };

    // todo rename to bootstrap, or any better name -> maybe something with 'engage'(r)
    @NonNull
    @Builder.Default
    private final IModuleContext<T> bootloader = (api, module, manager) -> {
    };

    @NonNull
    @Builder.Default
    private final IModuleContext<T> uninstaller = (api, module, manager) -> {
    };

    @NonNull
    @Builder.Default
    private final IModuleContext<T> update = (api, module, manager) -> {
    };

    public void fireInternalContext(String contextType, T api, Module<T> module, IEngine<T> manager) {
        if (this.internalContexts.containsKey(contextType)) {
            this.internalContexts.get(contextType).forEach(context -> context.fire(api, module, manager));
        }
    }

    public void addInternalContext(String contextType, IModuleContext<T> context) {
        if (!this.internalContexts.containsKey(contextType)) {
            this.internalContexts.put(contextType, List.of(context));
        } else {
            this.internalContexts.get(contextType).add(context);
        }
    }

    public Shard getShard(Class<?> shardClass) {
        return this.getShardManager().get(shardClass);
    }

    public <V> V getShardInstance(Class<V> shardClass) {
        return this.getShardManager().get(shardClass).getInstance();
    }

    @Deprecated
    public boolean hasClassInstance() {
        return this.state.containsKey("class");
    }

    @Deprecated
    @SuppressWarnings("UnusedReturnValue")
    public <V extends IBaseModuleClass<T>> V classInstance(@Nullable final V base) {
        return this.state.getOrSet("class", base);
    }

    @Deprecated
    @SuppressWarnings("UnusedReturnValue")
    public <V extends IBaseModuleClass<T>> V classInstance() {
        return this.classInstance(null);
    }

    @Deprecated
    public <V extends IBaseModuleClass<T>> Optional<V> optionalClassInstance() {
        return Optional.of(this.state.get("class"));
    }

    @Override
    public @NonNull Module<T> me() {
        return this;
    }

    // todo make better solution -> add shard truffle pig (extract details from shards) like contexts
    {
        this.addInternalContext(BasicModuleContextType.INSTALL.getBaseType(), (api, module, manager) -> {
            module.getShardManager().getAllInstanceHoldingShards().forEach(shard -> {
                if (IBaseModuleClass.class.isAssignableFrom(shard.shardClass())) {
                    final IBaseModuleClass<T> instance = shard.getInstance();
                    instance.install(api, module, manager);
                }
            });
        });

        this.addInternalContext(BasicModuleContextType.ENGAGE.getBaseType(), (api, module, manager) -> {
            module.getShardManager().getAllInstanceHoldingShards().forEach(shard -> {
                if (IBaseModuleClass.class.isAssignableFrom(shard.shardClass())) {
                    final IBaseModuleClass<T> instance = shard.getInstance();
                    instance.boot(api, module, manager);
                }
            });
        });

        this.addInternalContext(BasicModuleContextType.DISENGAGE.getBaseType(), (api, module, manager) -> {
            module.getShardManager().getAllInstanceHoldingShards().forEach(shard -> {
                if (IBaseModuleClass.class.isAssignableFrom(shard.shardClass())) {
                    final IBaseModuleClass<T> instance = shard.getInstance();
                    instance.uninstall(api, module, manager);
                }
            });
        });
    }
}
