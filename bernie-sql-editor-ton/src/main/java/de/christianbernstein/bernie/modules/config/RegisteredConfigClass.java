package de.christianbernstein.bernie.modules.config;

import lombok.Builder;
import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class RegisteredConfigClass {

    private Class<?> configClass;

    private String configName;

    private IConfigGenerator<?> generator;
}
