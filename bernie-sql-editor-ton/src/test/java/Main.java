import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Main {

    public void main(String[] args) {

        // Store the number of executions
        final AtomicInteger count = new AtomicInteger();

        // Call a recursive function, with a callback to count the number of executions
        rec(100, count::getAndIncrement);

        System.out.println(count.get());
    }

    public void rec(int n, @NotNull Runnable callback) {
        callback.run();
        if (n > 1) {
            rec(n - 1, callback);
        }
    }
}
