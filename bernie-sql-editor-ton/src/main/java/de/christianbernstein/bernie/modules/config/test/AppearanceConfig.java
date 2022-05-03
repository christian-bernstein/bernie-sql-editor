package de.christianbernstein.bernie.modules.config.test;

import de.christianbernstein.bernie.modules.config.ConfigDeclaration;
import de.christianbernstein.bernie.modules.config.ConfigGenerator;
import de.christianbernstein.bernie.modules.config.Element;
import de.christianbernstein.bernie.modules.config.IConfigGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Christian Bernstein
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "appearance_config")
@ConfigDeclaration(name = "appearance_config")
public class AppearanceConfig {

    @Id
    private String id;

    @Element(name = "theme")
    private Theme theme;

    @Element(name = "monospace")
    private boolean monospace;

    @ConfigGenerator
    private static final IConfigGenerator<AppearanceConfig> generator = user -> AppearanceConfig.builder()
            .id(user.getID())
            .monospace(true)
            .theme(Theme.LIGHT)
            .build();
}
