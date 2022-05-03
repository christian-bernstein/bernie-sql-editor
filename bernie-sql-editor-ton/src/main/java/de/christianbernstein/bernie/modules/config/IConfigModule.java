package de.christianbernstein.bernie.modules.config;

import de.christianbernstein.bernie.sdk.module.IBaseModuleClass;
import de.christianbernstein.bernie.sdk.module.Module;
import de.christianbernstein.bernie.sdk.module.ModuleDefinition;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.ITon;

/**
 * @author Christian Bernstein
 */
public interface IConfigModule extends IBaseModuleClass<ITon> {

    @ModuleDefinition(into = Constants.tonEngineID)
    Module<ITon> configModule = Module.<ITon>builder()
            .name("config_module")
            .build()
            .$(module -> module.getShardManager().install(ConfigModule.class));

    IConfig loadConfig(String userID, String configID) throws Exception;

}
