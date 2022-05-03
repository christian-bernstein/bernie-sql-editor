package de.christianbernstein.bernie.modules.config.test;

import de.christianbernstein.bernie.modules.config.ConfigModule;
import de.christianbernstein.bernie.modules.user.UserModule;
import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.ITon;

/**
 * @author Christian Bernstein
 */
public class Unit1 {

    @UseTon
    private static ITon ton;

    /**
     * User wants to set his theme to dark theme
     */
    @AutoExec
    public static void unit() throws Exception {
        ton.require(ConfigModule.class, ConfigModule.configModule.getName())
                .loadConfig(ton.userModule().root().getID(), "appearance_config")
                .<Theme>get("theme").update(Theme.DARK);
    }
}
