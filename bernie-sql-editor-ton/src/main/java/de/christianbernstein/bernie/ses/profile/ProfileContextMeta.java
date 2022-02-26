package de.christianbernstein.bernie.ses.profile;

import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@Data
public class ProfileContextMeta {

    @Nullable
    private String viewerID;

    @NonNull
    private String targetID;

    @NonNull
    private IProfileModule module;
}
