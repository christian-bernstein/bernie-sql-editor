package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Getter
@RequiredArgsConstructor
public class Config implements IConfig {

    private final String configName;

    private final H2Repository<?, Serializable> repository;

    private final List<IConfigElement<?>> configElements;

    private final String associatedUserID;

    @SuppressWarnings("unchecked")
    @Override
    public <T> IConfigElement<T> get(@NonNull String configElementName) {
        return (IConfigElement<T>) this.getConfigElements().stream().filter(elem -> elem.getElementName().equals(configElementName)).findAny().orElseThrow();
    }
}
