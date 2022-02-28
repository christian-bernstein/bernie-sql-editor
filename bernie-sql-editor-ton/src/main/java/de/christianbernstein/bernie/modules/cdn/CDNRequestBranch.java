package de.christianbernstein.bernie.modules.cdn;

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

    /**
     * used to identify a user, if cdn not for a user, this variable will bu null
     */
    @Nullable
    private String targetID;

    @Nullable
    private String requestID;
}
