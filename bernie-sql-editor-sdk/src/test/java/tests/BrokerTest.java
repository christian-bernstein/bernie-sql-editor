package tests;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import de.christianbernstein.bernie.sdk.misc.SafeRunnable;
import de.christianbernstein.bernie.sdk.misc.Unsafe;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
public class BrokerTest {

    private static final String EXCHANGE_NAME = "basic";

    @FunctionalInterface
    interface IBrokerAction {
        void run(@NonNull Channel channel) throws Exception;
    }

    public static void useBroker(@NonNull String host, @NonNull final IBrokerAction action) {
        final ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(host);

        try (final Connection connection = cf.newConnection(); final Channel channel = connection.createChannel()) {
            action.run(channel);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        new Sub(EXCHANGE_NAME, (s, delivery) -> System.out.println("Received: " + new String(delivery.getBody(), StandardCharsets.UTF_8)));

        new Pub().send("Hello world");
    }

    static class Pub {

        @NonNull
        private final Channel channel;

        public Pub() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            this.channel = connection.createChannel();
        }

        public Pub send(final byte[] bytes) throws IOException {
            this.channel.basicPublish(BrokerTest.EXCHANGE_NAME, "", null, "hello world".getBytes(StandardCharsets.UTF_8));
            return this;
        }

        public Pub send(final @NotNull String message) throws IOException {
            return this.send(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    static class Sub {

        @NonNull
        private final Channel channel;

        public Sub(@NonNull String exchange, @NonNull final DeliverCallback dc) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            this.channel = connection.createChannel();
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchange, "");
            this.channel.basicConsume(queueName, true, dc, consumerTag -> { });
        }
    }
}
