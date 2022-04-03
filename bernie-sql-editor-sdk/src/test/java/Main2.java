import de.christianbernstein.bernie.sdk.hermes.Hermes;
import de.christianbernstein.bernie.sdk.hermes.HttpContext;
import de.christianbernstein.bernie.sdk.hermes.HttpContextHandler;
import de.christianbernstein.bernie.sdk.hermes.Server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * @author Christian Bernstein
 */
@Server
public class Main2 {

    @HttpContext("ton")
    private final HttpContextHandler ton = request -> {
    };

    @HttpContext("hello")
    private final HttpContextHandler hello = request -> {
        final byte[] bytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        request.exchange().sendResponseHeaders(200, bytes.length);
        request.exchange().getResponseBody().write(bytes);
    };

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        new Hermes(Main2.class).start(Executors.newSingleThreadExecutor());

        System.out.println(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8000/hello"))
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("Sample request body"))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
    }
}
