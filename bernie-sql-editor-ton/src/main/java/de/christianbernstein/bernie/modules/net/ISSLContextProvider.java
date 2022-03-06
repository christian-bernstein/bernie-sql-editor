package de.christianbernstein.bernie.modules.net;

import javax.net.ssl.SSLContext;

/**
 * @author Christian Bernstein
 */
@FunctionalInterface
public interface ISSLContextProvider {

    SSLContext load(INetModule module);

}
