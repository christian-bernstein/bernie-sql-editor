package de.christianbernstein.bernie.ses.resolver;

import de.christianbernstein.bernie.modules.cdn.CDN;
import de.christianbernstein.bernie.modules.cdn.ICDNResolver;
import de.christianbernstein.bernie.modules.cdn.models.ContextualLink;
import de.christianbernstein.bernie.modules.cdn.models.ImageData;
import de.christianbernstein.bernie.modules.cdn.models.ImageDataType;
import de.christianbernstein.bernie.modules.profile.*;
import de.christianbernstein.bernie.modules.user.UserData;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class ProfileResolvers {

    @CDN(id = "hotfix-upd")
    private final ICDNResolver<UserPublicProfileData> hotfixUserPublicProfileDataResolver = (branch, request, user, ton) -> {
        try {
            final String targetID = branch.getTargetID();
            assert targetID != null;

            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "hotfix-upd", String.format("Load UserData from id '%s'", targetID));

            final UserData userData = ton.userModule().getUserDataOf(targetID);
            final IProfileModule module = ton.profileModule();
            final IProfileContext context = module.context(user == null ? null : user.getID(), targetID);
            final String email = userData.getEmail();
            final String username = userData.getUsername();
            final String firstname = userData.getFirstname();
            final String lastname = userData.getLastname();
            final Date lastActive = new Date();
            final ImageData profilePicture = new ImageData(ImageDataType.LINK, "https://i.pinimg.com/originals/5d/1b/b3/5d1bb3546868cf3791880fa5720da7a2.gif");
            final ImageData banner = new ImageData(ImageDataType.LINK, "https://i.pinimg.com/originals/5d/1b/b3/5d1bb3546868cf3791880fa5720da7a2.gif");
            final String biography = context.getBiography();
            final List<ContextualLink> links = new ArrayList<>();
            final UserActiveState activeState = UserActiveState.OFFLINE;
            final ClientDeviceType deviceType = ClientDeviceType.UNKNOWN;
            final List<ProfileBadgeData> badges = new ArrayList<>();
            final String viewedFromID = request.getViewerID();

            return UserPublicProfileData.builder()
                    .id(targetID)
                    .email(email)
                    .username(username)
                    .firstname(firstname)
                    .lastname(lastname)
                    .lastActive(lastActive)
                    .profilePicture(profilePicture)
                    .banner(banner)

                    // .biography(biography)
                    .biography("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s")

                    .links(links)
                    .activeState(activeState)
                    .deviceType(deviceType)
                    .badges(badges)
                    .viewedFromID(viewedFromID)
                    .build();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    };
}
