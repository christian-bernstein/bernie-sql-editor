package de.christianbernstein.bernie.modules.cdn;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.modules.user.IUser;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
public class CDNModule implements ICDNModule {

    @UseTon
    private static ITon ton;

    private final Map<CDN, ICDNResolver<?>> resolvers = new HashMap<>();

    @Override
    public CDNResponse request(@NonNull CDNRequest request) {
        final String viewerID = request.getViewerID();
        final List<CDNRequestBranch> branches = request.getBranches();
        final List<CDNResponseEntry> responseEntries = new ArrayList<>();
        final IUser user = viewerID == null ? null : ton.userModule().getUser(request.getViewerID());

        branches.forEach(branch -> {
            final String id = branch.getBranch();
            this.resolvers.keySet().stream().filter(cdn -> cdn.id().equals(id)).findAny().ifPresent(cdn -> {
                final ICDNResolver<?> resolver = this.resolvers.get(cdn);
                final List<Exception> errors = new ArrayList<>();
                Object resolved = null;
                CDNStatusCode status;
                try {
                    resolved = resolver.resolve(branch, request, user, ton);
                    status = CDNStatusCode.OK;
                } catch (final Exception e) {
                    ton.ifDebug(e::printStackTrace);
                    errors.add(e);
                    status = CDNStatusCode.UNKNOWN_ERROR;
                }
                responseEntries.add(CDNResponseEntry.builder().data(resolved).errors(errors).status(status).requestID(branch.getRequestID()).build());
            });
        });

        return CDNResponse.builder().entries(responseEntries).build();
    }

    @Override
    public void registerResolverFromAnnotation(ICDNResolver<?> resolver, CDN annotation) {
        this.resolvers.putIfAbsent(annotation, resolver);
    }
}
