package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.ITon;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public class ConfigModule implements IConfigModule {

    @UseTon
    private static ITon ton;

    private static final Map<String, RegisteredConfigClass> registeredConfigClasses = new HashMap<>();

    @Override
    public IConfig loadConfig(String userID, String configID) throws Exception {
        final RegisteredConfigClass configClass = ConfigModule.registeredConfigClasses.get(configID);
        final Centralized<? extends H2Repository<?, Serializable>> centralized = ton.db(configClass.getConfigClass());
        final H2Repository<?, Serializable> repository = centralized.get();
        Object object = repository.get(userID);

        if (object == null) {
            this.generateConfig(userID, configClass, repository);
            object = repository.get(userID);
        }

        // todo handle case
        assert object != null;

        final Map<Element, Object> values = this.loadValuesFromObject(object);


        return null;
    }

    private void generateConfig(String userID, @NotNull RegisteredConfigClass configClass, @NotNull H2Repository<?, Serializable> repository) {
        final Object config = configClass.getGenerator().generate(ton.userModule().getUser(userID));
        repository.saveObject(config);
    }

    private @NotNull Map<Element, Object> loadValuesFromObject(@NotNull Object object) throws IllegalAccessException {
        final Map<Element, Object> values = new HashMap<>();
        for (final Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Element.class)) {
                final Element element = field.getAnnotation(Element.class);
                final Object val = field.get(object);
                values.put(element, val);
            }
        }
        return values;
    }
}
