package de.christianbernstein.bernie.ses.profile;

import de.christianbernstein.bernie.ses.cdn.models.ContextualLink;
import de.christianbernstein.bernie.ses.cdn.models.ImageData;
import lombok.NonNull;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bernstein
 */
public interface IProfileContext {

    @NonNull
    ProfileContextMeta meta();

    ImageData getProfilePicture();

    ImageData getBanner();

    String getBiography();

    List<ContextualLink> getLinks();

    List<ProfileBadgeData> getBadges();

    Date getLastActive();
}
