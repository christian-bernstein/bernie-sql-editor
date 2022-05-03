package de.christianbernstein.bernie.modules.config;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface IConfigElementUpdater<T> {

    T update(IConfig config, IConfigElement<T> element, T current);

}
