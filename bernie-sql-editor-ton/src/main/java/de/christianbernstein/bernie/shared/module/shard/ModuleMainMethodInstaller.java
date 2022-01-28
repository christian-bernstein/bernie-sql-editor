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

import lombok.NonNull;

import de.christianbernstein.bernie.shared.module.Module;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Christian Bernsttein
 */
public class ModuleMainMethodInstaller<API> implements IShardInstaller<API> {

    @Override
    public ShardInstallerResult install(@NonNull ShardInstallContext context, @NonNull Module<API> module, @NonNull Class<?> shardClass) {
        // todo replace with ShardUtils
        if (context.shard() == null) {
            try {
                final Constructor<?> constructor = shardClass.getConstructor();
                constructor.setAccessible(true);
                final Object instance;
                instance = constructor.newInstance();
                context.shard(instance);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return ShardInstallerResult.merge(context).installable(false);
            }
        }
        return ShardInstallerResult.merge(context);
    }
}
