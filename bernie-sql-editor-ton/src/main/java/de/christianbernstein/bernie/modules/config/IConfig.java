package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import de.christianbernstein.bernie.sdk.misc.IFluently;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public interface IConfig extends IFluently<IConfig> {

    String getAssociatedUserID();

    String getConfigName();

    String toJSONString();

    Map<String, Object> toMap();

    RegisteredConfigClass getRegisteredConfigClass();

    H2Repository<?, Serializable> getRepository();

    List<IConfigElement<?>> getConfigElements();

    <T> IConfigElement<T> get(@NonNull String configElementName);
}
