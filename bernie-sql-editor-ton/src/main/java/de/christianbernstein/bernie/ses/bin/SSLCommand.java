package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.modules.net.NetModuleConfigShard;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.annotations.CommandClass;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.Command;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;

import static de.christianbernstein.bernie.ses.bin.ConsoleColors.*;

/**
 * @author Christian Bernstein
 */
@CommandClass
public class SSLCommand {

    @UseTon
    private static ITon ton;

    @Command(literal = "ssl", description = "Shows whether the current net module supports SSL connections or not")
    private void ssl() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ssl", String.format("using SSL: %s", ton.netModule().isUsingSSL() ?
                ConsoleColors.confined(GREEN, "yes") :
                ConsoleColors.confined(RED_BRIGHT, "no")
        ));
    }

    @Command(path = "ssl", literal = "enable", aliases = {"e"})
    private void enable() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ssl", "enabling ssl");
        setSSL(true);
    }

    @Command(path = "ssl", literal = "disable", aliases = {"d"})
    private void disable() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ssl", "disabling ssl");
        setSSL(false);
    }

    private void setSSL(boolean mode) {
        SSLCommand.ton.config(NetModuleConfigShard.class, "net_config", NetModuleConfigShard.builder().build()).update(conf -> {
            conf.setSsl(mode);
            return conf;
        });
        ConsoleLogger.def().log(ConsoleLogger.LogType.WARN, "ssl", "To make the change get into action, dis- & re-engage the 'net_module'-module");
    }
}
