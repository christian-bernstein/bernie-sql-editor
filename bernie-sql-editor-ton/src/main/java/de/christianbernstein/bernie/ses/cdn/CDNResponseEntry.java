package de.christianbernstein.bernie.ses.cdn;

import lombok.Builder;
import lombok.Data;

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

}
