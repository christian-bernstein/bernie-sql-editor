package de.christianbernstein.bernie.modules.config;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Christian Bernstein
 */
public interface IConfigElement<T> {

    void setConfig(Supplier<IConfig> configSupplier);

    IConfig getConfig();

    String getElementName();

    T get();

    void update(T newValue);

    void update(IConfigElementUpdater<T> updater);

    String getSQlDatatype();

    boolean isUsingSQLDatatypeLength();

    short getSQLDatatypeLength();
}
