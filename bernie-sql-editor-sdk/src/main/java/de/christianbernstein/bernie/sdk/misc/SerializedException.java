package de.christianbernstein.bernie.sdk.misc;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class SerializedException {

    private String type;

    private String message;

    private Map<String, Object> data;
}
