package de.christianbernstein.bernie.ses.cdn.test;

import de.christianbernstein.bernie.ses.cdn.CDN;
import de.christianbernstein.bernie.ses.cdn.ICDNResolver;

/**
 * @author Christian Bernstein
 */
public class TestResolver {

    @CDN(id = "rnd_mum")
    public static ICDNResolver<Integer> rndNumGen = (branch, request, user) -> {
        return 1;
    };

}
