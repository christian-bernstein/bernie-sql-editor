package de.christianbernstein.bernie.modules.profile;

import de.christianbernstein.bernie.modules.cdn.models.ContextualLink;
import de.christianbernstein.bernie.modules.cdn.models.ImageData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Bernstein
 */
@RequiredArgsConstructor
public class ProfileContext implements IProfileContext {

    @Getter
    @Accessors(fluent = true)
    private final ProfileContextMeta meta;

    @Override
    public ImageData getProfilePicture() {
        return null;
    }

    @Override
    public ImageData getBanner() {
        return null;
    }

    @Override
    public String getBiography() {
        final BiographyMapping mapping = this.meta()
                .module()
                .getBiographyRepo()
                .get()
                .get(this.meta.targetID());
        if (mapping == null) {
            // todo init the bio
            return "";
        } else {
            try {
                return mapping.getBiography();
            } catch (final Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    @Override
    public List<ContextualLink> getLinks() {
        return null;
    }

    @Override
    public List<ProfileBadgeData> getBadges() {
        return null;
    }

    @Override
    public Date getLastActive() {
        return null;
    }
}
