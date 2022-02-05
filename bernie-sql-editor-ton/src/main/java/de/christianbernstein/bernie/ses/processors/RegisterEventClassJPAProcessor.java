package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.RegisterEventClass;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * todo implement
 * @author Christian Bernstein
 */
@UtilityClass
public class RegisterEventClassJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = RegisterEventClass.class, phases = Constants.registerEventClassJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor registerEventClassJPAProcessor = (annotation, at, meta, instance) -> {
        final RegisterEventClass definition = (RegisterEventClass) annotation;

        if (instance == null) {

        }

    };
}
