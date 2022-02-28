package de.christianbernstein.bernie.modules.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@Data
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ProfileContextMeta {

    @Nullable
    private String viewerID;

    @NonNull
    private String targetID;

    @NonNull
    private IProfileModule module;
}
