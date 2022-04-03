import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Christian Bernstein
 */
public class Main1 {

    public static HttpServer server(InetSocketAddress address, boolean ssl) {
        final HttpServer server;
        try {
            server = ssl ? HttpsServer.create(address, 0) : HttpServer.create(address, 0);
            server.createContext("/ton", exchange -> {
                final InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                final BufferedReader br = new BufferedReader(isr);
                final String value = br.readLine();
            });
            return server;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {

        final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/ton", exchange -> {
            final InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            final BufferedReader br = new BufferedReader(isr);
            final String value = br.readLine();

        });

        final ExecutorService executor = Executors.newSingleThreadExecutor();

        server.setExecutor(executor);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Shutdown executor service");
            executor.shutdownNow();
        }).start();

    }
}
