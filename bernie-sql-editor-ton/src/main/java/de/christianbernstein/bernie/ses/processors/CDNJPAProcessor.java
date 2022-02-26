package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.cdn.CDN;
import de.christianbernstein.bernie.ses.cdn.ICDNResolver;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class CDNJPAProcessor {

    @UseTon
    private ITon ton;

    @JavaReflectiveAnnotationAPI.JRP(type = CDN.class, phases = Constants.cdnJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor cdnObjectJPAProcessor = (annotation, at, field, meta, instance) -> {
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

    @JavaReflectiveAnnotationAPI.JRP(type = CDN.class, phases = Constants.cdnJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor cdnClassJPAProcessor = (annotation, at, meta, instance) -> {
        final CDN cdn = (CDN) annotation;
        if (ICDNResolver.class.isAssignableFrom(at)) {
            if (instance != null) {
                ton.cdnModule().registerResolverFromAnnotation((ICDNResolver<?>) instance, cdn);
            } else {
                try {
                    final Constructor<?> constructor = at.getConstructor();
                    final ICDNResolver<?> resolver = (ICDNResolver<?>) constructor.newInstance();
                    ton.cdnModule().registerResolverFromAnnotation(resolver, cdn);
                } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
