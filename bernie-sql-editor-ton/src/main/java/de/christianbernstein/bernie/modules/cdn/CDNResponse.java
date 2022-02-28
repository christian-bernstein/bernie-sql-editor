package de.christianbernstein.bernie.modules.cdn;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class CDNResponse {

    private List<CDNResponseEntry> entries;

}
