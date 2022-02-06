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

package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ModuleJPAProcessor {

    @UseTon
    private ITon ton;

    @JavaReflectiveAnnotationAPI.JRP(type = ModuleDefinition.class, phases = Constants.moduleJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor moduleJPAProcessor = (annotation, at, field, meta, instance) -> {
        final ModuleDefinition definition = (ModuleDefinition) annotation;
        try {
            final Module<?> module = (Module<?>) field.get(null);
            try {
                if (module != null) {
                    ton.engine().register(definition.autoEngage(), module);
                } else {
                    System.err.printf("Module field at '%s' is null\n", definition);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
    };
}
