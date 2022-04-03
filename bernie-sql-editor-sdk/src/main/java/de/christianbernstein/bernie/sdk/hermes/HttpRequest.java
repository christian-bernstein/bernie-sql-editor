package de.christianbernstein.bernie.sdk.hermes;

import com.sun.net.httpserver.HttpExchange;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
public class HttpRequest {

    private final HttpExchange exchange;

}
