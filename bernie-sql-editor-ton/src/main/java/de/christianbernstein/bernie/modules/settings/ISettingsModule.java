package de.christianbernstein.bernie.modules.settings;

import de.christianbernstein.bernie.modules.session.SessionModule;
import de.christianbernstein.bernie.sdk.misc.IFluently;
import de.christianbernstein.bernie.sdk.module.IBaseModuleClass;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.sdk.module.ModuleDefinition;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;

/**
 * @author Christian Bernstein
 */
public interface ISettingsModule extends IBaseModuleClass<ITon>, IFluently<ISettingsModule> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> settingsModule = Module.<ITon>builder()
            .name("settings_module")
            .build()
            .$(module -> module.getShardManager().install(SettingsModule.class));


}
