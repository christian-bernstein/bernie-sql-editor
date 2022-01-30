package de.christianbernstein.bernie.ses;

import lombok.Builder;
import lombok.Data;

/**
 * A client is a representation of a thing or person who issued a certain action.
 *
 * @author Christian Bernstein
 */
@Data
@Builder
public class Client {

    private final String id;

    private final ClientType type;

    private final String username;
}
