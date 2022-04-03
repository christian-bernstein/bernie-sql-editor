package de.christianbernstein.bernie.sdk.hermes;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import lombok.Data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * @author Christian Bernstein
 */
@Data
public class Hermes implements IHermes<Hermes> {

    private final Class<?> serverClass;

    private HttpServer server;

    private Object httpServerClassInstance;

    @Override
    public Hermes start(ExecutorService executor) {
        if (this.serverClass.isAnnotationPresent(Server.class)) {
            final Server server = this.serverClass.getAnnotation(Server.class);
            final InetSocketAddress address = new InetSocketAddress(server.port());

            try {
                final Constructor<?> constructor = this.serverClass.getConstructor();
                this.httpServerClassInstance = constructor.newInstance();
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                this.server = server.ssl() ? HttpsServer.create(address, 0) : HttpServer.create(address, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (final Field field : this.serverClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(HttpContext.class) && HttpContextHandler.class.isAssignableFrom(field.getType())) {
                    final HttpContext context = field.getAnnotation(HttpContext.class);
                    field.setAccessible(true);
                    final HttpContextHandler hch;
                    try {
                        hch = (HttpContextHandler) field.get(this.httpServerClassInstance);
                        this.server.createContext(String.format("%s/%s", server.basePath(), context.value()), exchange -> {
                            hch.handle(HttpRequest.builder().exchange(exchange).build());
                        });
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.server.start();
        }
        return this;
    }

    public HttpsServer httpsServer() {
        return (HttpsServer) server;
    }
}
