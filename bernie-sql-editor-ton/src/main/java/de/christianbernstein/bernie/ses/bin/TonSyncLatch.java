package de.christianbernstein.bernie.ses.bin;

import lombok.Getter;

/**
 * @author Christian Bernstein
 */
public enum TonSyncLatch {

    OPEN("open"), CLOSE("close");

    @Getter
    private final String name;

    TonSyncLatch(String name) {
        this.name = name;
    }
}
