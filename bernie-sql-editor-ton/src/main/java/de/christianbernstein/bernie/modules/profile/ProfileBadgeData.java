package de.christianbernstein.bernie.modules.profile;

import lombok.Data;

import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
public class ProfileBadgeData {

    private String type;

    private Map<String, Object> attributes;

}
