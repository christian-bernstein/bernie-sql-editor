package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.annotations.Construct;
import de.christianbernstein.bernie.ses.annotations.Initializer;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.misc.Instance;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI.*;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ConstructJPAProcessor {

    /**
     * todo add @Initializer(timing = Timing.POST) -> post feature
     * todo handle if the instance variable is not null. t.m. the class was already constructed in any way (maybe not the intended one..)
     */
    @JRP(type = Construct.class, phases = Constants.constructJRAPhase)
    public final IAnnotationAtClassProcessor constructJPAProcessor = (annotation, at, meta, instance) -> {
        try {
            final Constructor<?> constructor = at.getConstructor();
            constructor.setAccessible(true);
            final Object obj = constructor.newInstance();
            // Initialize the object, by calling its manually added initializers
            ConstructJPAProcessor.callInitializersFor(obj, at);
            // Set the newly constructed instance to all the @Instance-annotated fields with matching types within the class itself
            ConstructJPAProcessor.pushObjectToInstanceHolders(obj, at);
        } catch (final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    };

    private void callInitializersFor(Object obj, @NotNull Class<?> at) throws InvocationTargetException, IllegalAccessException {
        // Call the method-based initializers
        for (final Method method : at.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Initializer.class) && method.getParameterCount() == 0 && method.getReturnType().equals(Void.TYPE)) {
                method.setAccessible(true);
                method.invoke(obj);
            }
        }
        // Call the lambda-based initializers
        // todo implement
    }

    private void pushObjectToInstanceHolders(Object obj, @NotNull Class<?> at) throws IllegalAccessException {
        for (final Field field : at.getFields()) {
            if (field.isAnnotationPresent(Instance.class) && field.getType().isAssignableFrom(at)) {
                field.setAccessible(true);
                field.set(obj, obj);
            }
        }
    }
}
