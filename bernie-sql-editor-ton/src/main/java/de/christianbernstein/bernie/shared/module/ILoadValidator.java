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

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a condition, under which the module can engage.
 *
 * @param <T> Type of engine/module api
 */
@FunctionalInterface
public interface ILoadValidator<T> {

    String dependencyTags = "dependency-related";

    /**
     * @return Return a list of tags, to enable and disable validators.
     */
    @NonNull
    default List<String> getTags() {
        return Collections.emptyList();
    }

    boolean canLoad(@NonNull IEngine<T> engine, @NonNull Module<T> module);

    /**
     * Load validator, that checks if all the necessary dependencies are engaged
     */
    @NotNull
    @Contract(pure = true)
    static <T> ILoadValidator<T> createDependencyLoadValidator(String... additionalTags) {
        return new ILoadValidator<>() {

            @Override
            public boolean canLoad(@NonNull IEngine<T> engine, @NonNull Module<T> innerModule) {
                if (!engine.isDependencyChecking()) {
                    return true;
                }
                final List<Dependency> dependencies = innerModule.getDependencies();
                final List<String> effectiveDependencyNames = dependencies.stream()
                        .filter(dependency -> !dependency.isOptional())
                        .map(Dependency::getModule).collect(Collectors.toList());
                final List<String> engagedModuleNames = engine.getEngagedModules().stream().map(Module::getName).collect(Collectors.toList());
                return engagedModuleNames.containsAll(effectiveDependencyNames);
            }

            @Override
            public @NonNull List<String> getTags() {
                if (additionalTags == null) {
                    return Collections.singletonList(ILoadValidator.dependencyTags);
                }
                final String[] tags = Arrays.copyOf(additionalTags, additionalTags.length + 1);
                tags[tags.length - 1] = ILoadValidator.dependencyTags;
                return Arrays.asList(tags);
            }
        };
    }
}
