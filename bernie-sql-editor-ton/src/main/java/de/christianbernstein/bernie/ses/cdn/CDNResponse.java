package de.christianbernstein.bernie.ses.cdn;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class CDNResponse {

    private List<CDNResponseEntry> entries;

}
