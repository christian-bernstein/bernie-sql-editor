/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.modules.net;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.shared.discovery.websocket.SocketShutdownReason;
import de.christianbernstein.bernie.shared.discovery.websocket.packets.SocketSwitchProtocolDataPacket;
import de.christianbernstein.bernie.shared.discovery.websocket.server.ServerConfiguration;
import de.christianbernstein.bernie.shared.discovery.websocket.server.StandaloneSocketServer;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;
import de.christianbernstein.bernie.shared.misc.Resource;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.Module;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public class NetModule implements INetModule {

    @UseTon
    private static ITon ton;

    private final NetModuleConfig config = NetModuleConfig.builder()
            .serverConfiguration(ServerConfiguration.builder()
                    .setDefaultProtocolOnPostEstablish(true)
                    // todo support factory initiation
                    .defaultProtocol(Protocols.loginProtocol)
                    .standardProtocol(Constants.centralProtocolName, Protocols.centralProtocolFactory)
                    // todo refactor
                    .baseProtocol(Protocols.baseProtocolFactory)
                    .build())
            .syncClose(false)
            .build();

    private StandaloneSocketServer server;

    private static final Map<String, Class<? extends ISSLContextProvider>> sslContextProviders = Map.of(
            "letsencrypt", LetsencryptSSLContextProvider.class
    );

    @Getter
    @Accessors(fluent = true)
    private Resource<NetModuleConfigShard> configResource;

    private @Nullable SSLContext loadSSLContext() {
        final NetModuleConfigShard conf = configResource.use(false);

        if (conf.isSsl()) {
            final String providerType = conf.getSslProviderType();
            if (!sslContextProviders.containsKey(providerType)) {
                throw new IllegalArgumentException(String.format("SSL environment provider type '%s' isn't mapped to a SSL context provider. This might indicate a version mismatch or a typo in net_module's internal config at option 'sslProviderType'.", providerType));
            }
            final Class<? extends ISSLContextProvider> providerClass = sslContextProviders.get(providerType);
            try {
                final Constructor<? extends ISSLContextProvider> constructor = providerClass.getConstructor();
                constructor.setAccessible(true);
                final ISSLContextProvider provider = constructor.newInstance();
                return provider.load(this);
            } catch (final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new UnsupportedOperationException("Cannot load SSL context without enabling 'SSL' option in net_module's internal config.");
        }
    }

    @Override
    public void boot(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        INetModule.super.boot(api, module, manager);
        this.configResource = api.config(NetModuleConfigShard.class, "net_module", NetModuleConfigShard.builder().build());

        final NetModuleConfigShard conf = this.configResource.use(false);
        this.server = new StandaloneSocketServer(this.config.getServerConfiguration());

        try {
            if (conf.isSsl()) {
                final SSLContext context = this.loadSSLContext();
                assert context != null;
                this.server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
            }
        } catch (final Exception e) {
            new NetModuleSSLException("While enabling SSL, an error occurred", e).printStackTrace();
        }



        // Sync protocol changes to the client
        this.server.onPostEstablish((event, document) -> {
            event.session().getProtocolController().addProtocolChangeListener((from, to) -> {
                event.session().push(new SocketSwitchProtocolDataPacket(to));
            });
        });

        // Start the socket server
        this.server.start();
        if (this.config.isSyncOpen()) {
            try {
                this.server.syncOpen();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uninstall(ITon api, @NotNull Module<ITon> module, IEngine<ITon> manager) {
        INetModule.super.uninstall(api, module, manager);
        ConsoleLogger.def().log(
                ConsoleLogger.LogType.INFO,
                "central module",
                String.format("Uninstalling net-module '%s'", this.config.getServerConfiguration().address())
        );
        try {
            this.server.shutdown(SocketShutdownReason.CORE_SHUTDOWN);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        // todo remove sync close -> not implemented in the right manner here! -> sync in synced method flow, sync close after closing
        if (this.config.isSyncClose()) {
            try {
                this.server.syncClose();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.server = null;
            }
        } else {
            this.server = null;
        }
    }

    @Override
    public StandaloneSocketServer getSocketServer() {
        return this.server;
    }

    @Override
    public @NonNull INetModule me() {
        return this;
    }

}
