package de.christianbernstein.bernie.ses.cdn;

import lombok.Getter;

/**
 * @author Christian Bernstein
 */
public enum CDNStatusCode {

    OK(0),
    UNKNOWN_ERROR(-1);

    @Getter
    private final int code;

    CDNStatusCode(int code) {
        this.code = code;
    }
}
