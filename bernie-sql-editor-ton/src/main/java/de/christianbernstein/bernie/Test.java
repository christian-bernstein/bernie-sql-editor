package de.christianbernstein.bernie;

import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.annotations.Threaded;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Test {

    @Threaded
    private ExecutorService main;

    @AutoExec
    private void sayHello() {
        main.submit(() -> {});
    }
}
