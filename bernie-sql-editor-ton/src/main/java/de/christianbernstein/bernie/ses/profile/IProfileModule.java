package de.christianbernstein.bernie.ses.profile;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public interface IProfileModule {

    IProfileContext context(@Nullable String viewerID, @NonNull String targetID);

}
