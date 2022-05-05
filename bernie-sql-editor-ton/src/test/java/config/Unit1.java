package config;

import de.christianbernstein.bernie.sdk.shared.Theme;
import de.christianbernstein.bernie.ses.annotations.AutoExec;
import de.christianbernstein.bernie.ses.bin.FanoutProcedure;

/**
 * @author Christian Bernstein
 */
public class Unit1 {

    /**
     * User wants to set his theme to dark theme
     */
    @AutoExec(run = false)
    private static final FanoutProcedure fp = ton -> {
        ton.configModule()
                .loadConfig(ton.userModule().root().getID(), "appearance_config")
                .$(c -> c.<Theme>get("theme").update(Theme.LIGHT))
                .$(c -> System.out.println(c.toJSONString()))
                .$(c -> c.<Theme>get("theme").update(Theme.DARK))
                .$(c -> System.out.println(c.toJSONString()));
    };
}
