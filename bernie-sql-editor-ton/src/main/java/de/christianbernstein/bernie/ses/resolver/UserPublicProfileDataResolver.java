package de.christianbernstein.bernie.ses.resolver;

import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.ses.cdn.CDN;
import de.christianbernstein.bernie.ses.cdn.CDNRequest;
import de.christianbernstein.bernie.ses.cdn.CDNRequestBranch;
import de.christianbernstein.bernie.ses.cdn.ICDNResolver;
import de.christianbernstein.bernie.ses.profile.UserPublicProfileData;
import de.christianbernstein.bernie.ses.user.IUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
@CDN(id = "UserPublicProfileData")
public class UserPublicProfileDataResolver implements ICDNResolver<UserPublicProfileData> {

    @Override
    public UserPublicProfileData resolve(@NonNull CDNRequestBranch branch, @NonNull CDNRequest request, @Nullable IUser user, @NonNull ITon ton) throws Exception {
        return null;
    }
}
