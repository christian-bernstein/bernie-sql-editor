package de.christianbernstein.bernie.modules.cdn;

import de.christianbernstein.bernie.ses.bin.ITon;
import de.christianbernstein.bernie.modules.user.IUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Christian Bernstein
 */
public interface ICDNResolver<T> {

    T resolve(@NonNull CDNRequestBranch branch, @NonNull CDNRequest request, @Nullable IUser user, @NonNull ITon ton) throws Exception;

}
