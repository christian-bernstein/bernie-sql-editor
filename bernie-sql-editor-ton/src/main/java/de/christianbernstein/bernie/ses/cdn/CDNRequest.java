package de.christianbernstein.bernie.ses.cdn;

import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
public class CDNRequest {

    @Nullable
    private String viewerID;

    @NonNull
    private List<CDNRequestBranch> branches;
}
