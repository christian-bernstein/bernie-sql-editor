package de.christianbernstein.bernie.ses.resolver;

import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.modules.cdn.CDN;
import de.christianbernstein.bernie.modules.cdn.CDNRequest;
import de.christianbernstein.bernie.modules.cdn.CDNRequestBranch;
import de.christianbernstein.bernie.modules.cdn.ICDNResolver;
import de.christianbernstein.bernie.modules.profile.IProfileContext;
import de.christianbernstein.bernie.modules.profile.IProfileModule;
import de.christianbernstein.bernie.modules.user.IUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@CDN(id = "biography")
public class BiographyResolver implements ICDNResolver<String> {

    @Override
    public String resolve(@NonNull CDNRequestBranch branch, @NonNull CDNRequest request, @Nullable IUser user, @NonNull ITon ton) throws Exception {
        assert branch.getTargetID() != null;
        final IProfileModule module = ton.profileModule();
        final IProfileContext context = module.context(user == null ? null : user.getID(), branch.getTargetID());
        return context.getBiography();
    }
}
