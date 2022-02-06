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

package de.christianbernstein.bernie.ses.flow;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class FlowDefinitionJPAProcessor {

    @SuppressWarnings("unused")
    @UseTon
    private ITon ton;

    @JavaReflectiveAnnotationAPI.JRP(type = FlowDefinition.class, phases = Constants.flowJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor flowJPAProcessor = (annotation, at, field, meta, instance) -> {
        final FlowDefinition definition = (FlowDefinition) annotation;
        if (IFlow.class.isAssignableFrom(field.getType())) {
            try {
                field.setAccessible(true);
                final IFlow flow = (IFlow) field.get(instance);
                if (flow != null) {
                    ton.flowModule().registerModule(definition.name(), flow);
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            // todo handle
            System.err.println("Flow cannot be registered, annotated field's type isn't assignable to IFlow.class");
        }
    };
}
