package de.christianbernstein.bernie.ses.cdn;

import de.christianbernstein.bernie.ses.cdn.models.UserPublicProfileData;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public class CDNModule implements ICDNModule {

    private final Map<CDN, ICDNResolver<?>> resolvers = new HashMap<>();

    @Override
    public CDNResponse request(@NonNull CDNRequest request) {
        final String viewerID = request.getViewerID();
        final List<CDNRequestBranch> branches = request.getBranches();

        // branches.forEach(branch -> );

        return null;
    }

    @Override
    public void registerResolverFromAnnotation(ICDNResolver<?> resolver, CDN annotation) {
        this.resolvers.putIfAbsent(annotation, resolver);
    }
}
