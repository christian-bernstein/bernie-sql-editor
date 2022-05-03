package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.modules.user.IUser;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface IConfigGenerator<T> {

    T generate(IUser user);

}
