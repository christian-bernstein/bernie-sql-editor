package de.christianbernstein.bernie.modules.db;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@Accessors(fluent = true)
public class DBListenerID {

    private UUID id;

    private DBListenerType type;

    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

}
