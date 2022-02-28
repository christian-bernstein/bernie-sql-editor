package de.christianbernstein.bernie.modules.cdn;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class CDNResponseEntry {

    private CDNStatusCode status;

    private List<Exception> errors;

    private Object data;

    @Nullable
    private String requestID;
}
