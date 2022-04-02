package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.modules.net.NetModuleConfigShard;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import de.christianbernstein.bernie.sdk.misc.ObjectNotationLanguage;
import de.christianbernstein.bernie.sdk.misc.Utils;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
public class ConfiguratorMain implements IMain<Ton> {

    private Ton ton;

    private String pathSuffix;

    @Override
    public MainResult main(@NonNull Ton ton) {
        this.ton = ton;
        ton.installGlobalStringReplacers();
        pathSuffix = ton.arguments().getOr("path", "");
        folder("{root_dir}ssl");
        folder("{config_dir}");
        file("{root_dir}ton_config.{config_file_ext}", () -> ObjectNotationLanguage.YAML.getSerialAdapter().serialize(ton.configuration(), TonConfiguration.class));
        file("{config_dir}net_config.{config_file_ext}", () -> new Yaml().dump(NetModuleConfigShard.builder().build()));
        return new MainResult(0);
    }

    public void file(String path, @Nullable Supplier<String> def) {
        path = String.format("%s%s", this.pathSuffix, this.ton.interpolate(path));
        final File file = new File(path);

        if (this.ton.arguments().containsKey("reset") && file.exists()) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Deleting file '%s'", path));
            if (file.delete()) {
                ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Deleting file '%s' successfully", path));
            } else {
                ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "config", String.format("File '%s' can't be deleted", path));
            }
        }

        if (!file.exists()) {
            try {
                Utils.createFileIfNotExists(file);
                if (def != null) {
                    final String val = def.get();
                    ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Writing content to '%s' (%s bytes)", path, val.getBytes(StandardCharsets.UTF_8).length));
                    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write(val);
                        ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", "Writing process successfully finished");
                    } catch (final Exception e) {
                        ConsoleLogger.def().log(ConsoleLogger.LogType.ERROR, "config", "Writing process unsuccessful");
                        e.printStackTrace();
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("File '%s' created", path));
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Skipping '%s', file already exists", path));
        }
    }

    public void folder(String path) {
        path = String.format("%s%s", this.pathSuffix, this.ton.interpolate(path));
        final File file = new File(path);
        if (file.mkdirs()) {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Folder '%s' created", path));
        } else {
            ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "config", String.format("Skipping '%s', folder already exists", path));
        }
    }
}
