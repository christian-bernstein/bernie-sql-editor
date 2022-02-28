package de.christianbernstein.bernie.modules.cdn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@AllArgsConstructor
public class CDNRequest {

    @Nullable
    private String viewerID;

    @NonNull
    private List<CDNRequestBranch> branches;
}
