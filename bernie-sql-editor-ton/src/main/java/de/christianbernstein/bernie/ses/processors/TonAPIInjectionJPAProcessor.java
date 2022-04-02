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
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class TonAPIInjectionJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = UseTon.class, phases = Constants.useTonJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor useTonJPAProcessor = (annotation, at, field, meta, instance) -> {
        // Get the ton instance from the meta
        final ITon ton = meta.get("ton");
        if (ton != null) {
            // Direct setting of a ton object field
            if (ITon.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        new UnsupportedOperationException("Cannot set ton instance, because field isn't static (Exc is never thrown -> code gets executed.. this is only a hint and has to be refactored by future Chris)").printStackTrace();
                    }
                    field.set(instance, ton);
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
