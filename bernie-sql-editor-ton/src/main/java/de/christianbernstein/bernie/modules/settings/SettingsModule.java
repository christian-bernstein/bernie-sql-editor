package de.christianbernstein.bernie.modules.settings;

import lombok.NonNull;

/**
 * @author Christian Bernstein
 */
public class SettingsModule implements ISettingsModule {

    @Override
    public @NonNull ISettingsModule me() {
        return this;
    }
}
