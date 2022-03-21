package de.christianbernstein.bernie.modules.profile;

import de.christianbernstein.bernie.modules.cdn.models.ContextualLink;
import de.christianbernstein.bernie.modules.cdn.models.ImageData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicProfileData {

    private String id;

    private String email;

    private String username;

    private String firstname;

    private String lastname;

    private Date lastActive;

    private ImageData profilePicture;

    private ImageData banner;

    private String biography;

    private List<ContextualLink> links;

    private UserActiveState activeState;

    private ClientDeviceType deviceType;

    private List<ProfileBadgeData> badges;

    private String viewedFromID;

}
