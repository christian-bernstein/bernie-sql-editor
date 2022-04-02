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

import de.christianbernstein.bernie.sdk.event.EventAPI;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;


/**
 * The module api is based on {@link EventAPI}.
 * <p>
 * todo make able to add & call custom {@link IModuleContext} instances via a string identifier
 * todo make module class to extend from, when building a module -> ClassifiedModule
 * todo add IDocument<T> to {@link IModuleContext}
 * todo add lambda api to modules
 * todo make event pipeline to modules -> install event handler in modules, that get removed if the module gets uninstalled
 * todo create JRA loading -> @Module(into = {"module_name"}, autoInstall = true)
 * todo Predicated, if a module can be installed
 * todo add register method of modules (they get added, but not installed)
 */
@UtilityClass
@Deprecated(forRemoval = true)
public final class ModuleAPI {

    // todo implement
    @JavaReflectiveAnnotationAPI.JRP(type = ModuleDefinition.class, at = Module.class, phases = "a non-existing phase! ~ (Do not load this processor yet)")
    private static final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor moduleDeclarationProcessor = (annotation, at, field, meta, instance) -> {
    };
}
