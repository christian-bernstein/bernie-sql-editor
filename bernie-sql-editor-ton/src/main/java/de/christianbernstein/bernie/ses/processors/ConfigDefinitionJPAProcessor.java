package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.modules.config.ConfigDeclaration;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ConfigDefinitionJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = ConfigDeclaration.class, phases = Constants.configDefinitionJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor configDefinitionJPAProcessor = (annotation, at, meta, instance) -> {

    };
}
