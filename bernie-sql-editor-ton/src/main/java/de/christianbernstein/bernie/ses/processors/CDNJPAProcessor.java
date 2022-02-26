package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.cdn.CDN;
import de.christianbernstein.bernie.ses.cdn.ICDNResolver;
import de.christianbernstein.bernie.ses.flow.FlowDefinition;
import de.christianbernstein.bernie.ses.flow.IFlow;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class CDNJPAProcessor {

    @UseTon
    private ITon ton;

    @JavaReflectiveAnnotationAPI.JRP(type = CDN.class, phases = Constants.cdnJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor cdnJPAProcessor = (annotation, at, field, meta, instance) -> {
        final CDN cdn = (CDN) annotation;
        if (ICDNResolver.class.isAssignableFrom(field.getType())) {
            try {
                field.setAccessible(true);
                final ICDNResolver<?> resolver = (ICDNResolver<?>) field.get(instance);
                if (resolver != null) {
                    ton.cdnModule().registerResolverFromAnnotation(resolver, cdn);
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    };
}
