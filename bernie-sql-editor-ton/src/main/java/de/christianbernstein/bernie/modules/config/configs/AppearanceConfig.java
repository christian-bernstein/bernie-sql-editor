package de.christianbernstein.bernie.modules.config.configs;

import de.christianbernstein.bernie.modules.config.ConfigDeclaration;
import de.christianbernstein.bernie.modules.config.ConfigGenerator;
import de.christianbernstein.bernie.modules.config.Element;
import de.christianbernstein.bernie.modules.config.IConfigGenerator;
import de.christianbernstein.bernie.modules.user.IUser;
import de.christianbernstein.bernie.sdk.shared.Theme;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * @author Christian Bernstein
 */
@ToString
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "appearance_config")
@ConfigDeclaration(name = "appearance_config")
public class AppearanceConfig {

    @Id
    @Element(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Element(name = "theme")
    private Theme theme;

    @ConfigGenerator
    private static final IConfigGenerator<AppearanceConfig> generator = (IUser user) -> AppearanceConfig.builder()
            .id(user.getID())
            .theme(Theme.LIGHT)
            .build();
}
