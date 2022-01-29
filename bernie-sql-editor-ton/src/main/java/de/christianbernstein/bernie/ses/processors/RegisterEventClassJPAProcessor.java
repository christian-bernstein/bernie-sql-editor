package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.RegisterEventClass;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.module.ModuleDefinition;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class RegisterEventClassJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = RegisterEventClass.class, phases = Constants.registerEventClassJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor registerEventClassJPAProcessor = (annotation, at, meta, instance) -> {
        final ModuleDefinition definition = (ModuleDefinition) annotation;

        if (instance == null) {

        }

    };
}
