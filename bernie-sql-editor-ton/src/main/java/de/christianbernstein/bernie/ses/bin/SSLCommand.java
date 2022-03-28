package de.christianbernstein.bernie.ses.bin;


import ch.qos.logback.core.net.ssl.SSL;
import de.christianbernstein.bernie.modules.net.NetModuleConfigShard;
import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.Command;
import de.christianbernstein.bernie.shared.misc.ConsoleLogger;

/**
 * @author Christian Bernstein
 */
public class SSLCommand {

    @UseTon
    private static ITon ton;

    @Command(literal = "ssl", type = Command.Type.JUNCTION)
    private void ssl() {}

    @Command(path = "ssl", literal = "enable", type = Command.Type.HANDLER)
    private void enable() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ssl", "enabling ssl");
        SSLCommand.ton.config(NetModuleConfigShard.class, "net_config", NetModuleConfigShard.builder().build()).update(conf -> {
            conf.setSsl(true);
            return conf;
        });
    }

    @Command(path = "ssl", literal = "disable", type = Command.Type.HANDLER)
    private void disable() {
        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "ssl", "disabling ssl");
        SSLCommand.ton.config(NetModuleConfigShard.class, "net_config", NetModuleConfigShard.builder().build()).update(conf -> {
            conf.setSsl(false);
            return conf;
        });
    }
}
