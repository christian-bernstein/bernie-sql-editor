package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.modules.config.*;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.ses.bin.Constants;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ConfigDefinitionJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = ConfigDeclaration.class, phases = Constants.configDefinitionJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor configDefinitionJPAProcessor = (annotation, at, meta, instance) -> {
        // todo register configs
        final ConfigDeclaration config = (ConfigDeclaration) annotation;
        final AtomicReference<IConfigGenerator<?>> generator = new AtomicReference<>();
        Arrays.stream(at.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ConfigGenerator.class)).findAny().ifPresentOrElse(field -> {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    generator.set((IConfigGenerator<?>) field.get(instance));
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                new ConfigurationSetupException(String.format("Cannot load the config generator for configuration class '%s'. @ConfigGenerator's field is declared non-static, but has to be declared static.", at.getName())).printStackTrace();
            }
        }, () -> {
            new ConfigurationSetupException(String.format("Cannot load configuration class '%s'. No configuration generator is declared", at.getName())).printStackTrace();
        });

        if (generator.get() == null) {
            return;
        }

        ConfigModule.registerConfigClass(config.name(), RegisteredConfigClass.builder().configClass(at).configName(config.name()).generator(generator.get()).build());
    };
}
