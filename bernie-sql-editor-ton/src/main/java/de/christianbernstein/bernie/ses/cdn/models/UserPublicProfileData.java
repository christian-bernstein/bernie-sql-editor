package de.christianbernstein.bernie.ses.cdn.models;

import de.christianbernstein.bernie.ses.cdn.models.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@Data
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
