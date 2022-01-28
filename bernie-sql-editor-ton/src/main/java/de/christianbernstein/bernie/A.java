package de.christianbernstein.bernie;

import de.christianbernstein.bernie.ses.Construct;
import de.christianbernstein.bernie.ses.Initializer;
import de.christianbernstein.bernie.shared.misc.Instance;

/**
 * @author Christian Bernstein
 */
@Construct
public class A {

    @Instance
    private A instance;

    @Initializer
    private void init() {
        System.err.println("init()....");
    }
}
