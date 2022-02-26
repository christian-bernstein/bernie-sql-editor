package de.christianbernstein.bernie.ses.cdn;

import de.christianbernstein.bernie.ses.user.IUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public interface ICDNResolver<T> {

    T resolve(@NonNull CDNRequestBranch branch, @NonNull CDNRequest request, @Nullable IUser user) throws Exception;

}
