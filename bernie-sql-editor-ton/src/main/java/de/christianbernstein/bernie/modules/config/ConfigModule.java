package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.modules.config.in.GetConfigRequestPacketData;
import de.christianbernstein.bernie.modules.config.in.UpdateConfigRequestPacketData;
import de.christianbernstein.bernie.modules.config.out.GetConfigResponsePacketData;
import de.christianbernstein.bernie.modules.net.SocketLaneIdentifyingAttachment;
import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.discovery.websocket.Discoverer;
import de.christianbernstein.bernie.sdk.discovery.websocket.IPacketHandlerBase;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Centralized;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.bin.Shortcut;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
public class ConfigModule implements IConfigModule {

    @UseTon
    private static ITon ton;

    private static final Map<String, RegisteredConfigClass> registeredConfigClasses = new HashMap<>();

    public static void registerConfigClass(String name, RegisteredConfigClass configClass) {
        ConfigModule.registeredConfigClasses.put(name, configClass);
    }

    @Discoverer(packetID = "UpdateConfigRequestPacketData", datatype = UpdateConfigRequestPacketData.class, protocols =  Constants.coreProtocolName)
    private static final IPacketHandlerBase<UpdateConfigRequestPacketData> updateConfigHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String userID = ton.getUserFromSessionID(sli.getSessionID()).getID();

        data.getChanges().forEach(changelist -> {
            try {
                final IConfig config = ton.configModule().loadConfig(userID, changelist.getConfigName());
                changelist.forEach((key, value) -> config.get(key).update(value));
            } catch (final Exception e) {
                e.printStackTrace();
                // todo send error back
            }
        });
    };

    @Discoverer(packetID = "GetConfigRequestPacketData", datatype = GetConfigRequestPacketData.class, protocols =  Constants.coreProtocolName)
    private static final IPacketHandlerBase<GetConfigRequestPacketData> getConfigHandler = (data, endpoint, socket, packet, server) -> {
        final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
        final String userID = ton.getUserFromSessionID(sli.getSessionID()).getID();
        final Map<String, Map<String, Object>> configs = new HashMap<>();
        final IConfigModule module = ton.configModule();

        data.getConfigNames().forEach(name -> {
            try {
                final IConfig config = module.loadConfig(userID, name);
                configs.put(name, config.toMap());
            } catch (final Exception e) {
                e.printStackTrace();
                // todo send error back
            }
        });

        packet.respond(new GetConfigResponsePacketData(configs), endpoint);
    };

    @Override
    public IConfig loadConfig(String userID, String configID) throws Exception {
        final RegisteredConfigClass configClass = ConfigModule.registeredConfigClasses.get(configID);
        final Centralized<? extends H2Repository<?, Serializable>> centralized = ton.db(configClass.getConfigClass());
        final H2Repository<?, Serializable> repository = centralized.get();
        Object object = repository.get(userID);
        if (object == null) {
            this.generateConfig(userID, configClass, repository);
            object = repository.load(userID);
        }
        // todo handle case
        assert object != null;
        final Map<Element, Object> values = this.loadValuesFromObject(object);
        final List<IConfigElement<?>> elements = values.entrySet().stream().map(entry ->
                ConfigElement.builder()
                        .elementName(entry.getKey().name())
                        .value(entry.getValue())
                        .build()).collect(Collectors.toList()
        );
        final Config config = new Config(configClass.getConfigName(), repository, new ArrayList<>(), userID, configClass);
        config.addElements(elements);
        return config;
    }

    private void generateConfig(String userID, @NotNull RegisteredConfigClass configClass, @NotNull H2Repository<?, Serializable> repository) {
        final Object config = configClass.getGenerator().generate(ton.userModule().getUser(userID));
        repository.saveObject(config);
    }

    private @NotNull Map<Element, Object> loadValuesFromObject(@NotNull Object object) throws IllegalAccessException {
        final Map<Element, Object> values = new HashMap<>();
        for (final Field field : object.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Element.class)) {
                continue;
            }
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
