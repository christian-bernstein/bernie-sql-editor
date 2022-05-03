package de.christianbernstein.bernie.modules.config;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * @author Christian Bernstein
 */
public interface IConfigElement<T> {

    IConfig getConfig();

    String getElementName();

    T get();

    void update(T newValue);

    void update(IConfigElementUpdater<T> updater);

    String getSQlDatatype();

    boolean isUsingSQLDatatypeLength();

    short getSQLDatatypeLength();
}
