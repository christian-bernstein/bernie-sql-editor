package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.db.H2Repository;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * @author Christian Bernstein
 */
public interface IConfig {

    String getAssociatedUserID();

    String getConfigName();

    H2Repository<?, Serializable> getRepository();

    List<IConfigElement<?>> getConfigElements();

    <T> IConfigElement<T> get(@NonNull String configElementName);
}
