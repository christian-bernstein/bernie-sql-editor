package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.annotations.Threaded;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ThreadedJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = Threaded.class, phases = Constants.useTonJRAPhase, at = ExecutorService.class)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtFieldProcessor useTonJPAProcessor = (annotation, at, field, meta, instance) -> {
        final Threaded an = (Threaded) annotation;
        // Get the ton instance from the meta
        final ITon ton = meta.get("ton");
        if (ton != null) {
            field.setAccessible(true);
            try {
                if (!Modifier.isStatic(field.getModifiers())) {
                    new UnsupportedOperationException("Cannot set executor instance, because field isn't static (Exc is never thrown -> code gets executed.. this is only a hint and has to be refactored by future Chris)").printStackTrace();
                }
                field.set(instance, ton.pool(an.pool()));
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    };
}
