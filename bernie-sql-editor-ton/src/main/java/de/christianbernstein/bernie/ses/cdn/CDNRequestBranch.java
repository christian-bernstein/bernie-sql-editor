package de.christianbernstein.bernie.ses.cdn;

import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
public class CDNRequestBranch {

    @NonNull
    private String branch;

    @NonNull
    private Map<String, Object> attributes;

    @Nullable
    private String accessToken;
}
