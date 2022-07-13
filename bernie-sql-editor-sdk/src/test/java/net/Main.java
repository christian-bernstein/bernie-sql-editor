package net;

import de.christianbernstein.bernie.sdk.discovery.websocket.packets.Primitives;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.ServerConfiguration;
import de.christianbernstein.bernie.sdk.discovery.websocket.server.StandaloneSocketServer;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        final StandaloneSocketServer server = new StandaloneSocketServer(ServerConfiguration.builder().address(new InetSocketAddress(25565)).build());

        server.onPostEstablish((event, doc) -> {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    final ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "wmic cpu get loadpercentage");
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    event.session().push(new Primitives.StringPacketData(in.lines().collect(Collectors.joining(" ")).split(" +")[1]));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }, 1, 1, TimeUnit.SECONDS);
        });

        server.start();
        server.syncOpen();
    }
}
