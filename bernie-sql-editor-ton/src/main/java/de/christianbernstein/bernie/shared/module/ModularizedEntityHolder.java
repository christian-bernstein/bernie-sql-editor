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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
@RequiredArgsConstructor
public class ModularizedEntityHolder<T, V> {

    private final List<ModuleEntity<V>> entities = new ArrayList<>();

    @Getter
    private final String id;

    @Getter
    private final IEngine<T> manager;

    public List<V> normalize() {
        return this.entities.stream().map(ModuleEntity::getData).collect(Collectors.toList());
    }

    public List<V> filterModules(String... modules) {
        return this.entities.stream().filter(entity -> {
            for (final String module : modules) {
                if (entity.getModule().equals(module)) {
                    return true;
                }
            }
            return false;
        }).map(ModuleEntity::getData).collect(Collectors.toList());
    }

    public void removeModule(@NonNull String module) {
        this.entities.removeIf(entity -> entity.getModule().equals(module));
    }

    public ModularizedEntityHolder<T, V> add(V entity, String module) {
        this.entities.add(ModuleEntity.wrap(entity, module));
        return this;
    }

    public ModularizedEntityHolder<T, V> add(V entity) {
        return add(entity, this.manager.getCalibration());
    }
}
