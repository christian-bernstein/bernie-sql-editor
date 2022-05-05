/*
 * Copyright (C) 2022 Christian Bernstein
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

import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.ses.bin.FanoutException;
import de.christianbernstein.bernie.ses.bin.FanoutProcedure;
import de.christianbernstein.bernie.ses.bin.ITon;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class AutoExecJPAProcessor {

    @UseTon
    private ITon ton;

    @JavaReflectiveAnnotationAPI.JRP(type = AutoExec.class, phases = Constants.autoEcexJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtMethodProcessor autoExecJPAProcessor = (annotation, at, method, meta, instance) -> {
        final AutoExec exec = (AutoExec) annotation;
        if (!exec.run()) {
            return;
        }
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    };

    @JavaReflectiveAnnotationAPI.JRP(type = AutoExec.class, phases = Constants.autoEcexJRAPhase, at = FanoutProcedure.class)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor autoExecAtFieldJPAProcessor = (annotation, at, field, meta, instance) -> {
        final AutoExec exec = (AutoExec) annotation;
        if (!exec.run()) {
            return;
        }
        try {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers()) && instance == null) {
                new FanoutException("Cannot execute fanout procedure, if the field is not static and no instance is provided").printStackTrace();
                return;
            }
            final FanoutProcedure procedure = (FanoutProcedure) field.get(instance);
            // todo catch any exceptions
            procedure.run(ton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    };
}
