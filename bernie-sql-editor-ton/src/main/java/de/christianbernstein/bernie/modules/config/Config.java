package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.misc.ObjectNotationLanguage;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Getter
@ToString
@RequiredArgsConstructor
public class Config implements IConfig {

    private final String configName;

    private final H2Repository<?, Serializable> repository;

    private final List<IConfigElement<?>> configElements;

    private final String associatedUserID;

    private final RegisteredConfigClass registeredConfigClass;

    @Override
    public String toJSONString() {
        return ObjectNotationLanguage.JSON.getSerialAdapter().serialize(this.toMap(), Map.class);
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        this.configElements.forEach(element -> {
            map.put(element.getElementName(), element.get());
        });
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IConfigElement<T> get(@NonNull String configElementName) {
        return (IConfigElement<T>) this.getConfigElements().stream().filter(elem -> elem.getElementName().equals(configElementName)).findAny().orElseThrow();
    }

    public void addElements(@NonNull List<IConfigElement<?>> elements) {
        elements.forEach(element -> element.setConfig(() -> this));
        this.configElements.addAll(elements);
    }

    @Override
    public @NonNull IConfig me() {
        return this;
    }
}
