package de.christianbernstein.bernie.ses.bin;

import ch.qos.logback.classic.Level;
import de.christianbernstein.bernie.sdk.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.sdk.db.HBM2DDLMode;
import lombok.*;

import java.util.Arrays;
import java.util.List;

/**
 * Standard configuration for Ton.
 * Controls the advanced behavior of the Ton server.
 */
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TonConfiguration {

    @NonNull
    private String workingDirectory;

    @Builder.Default
    private TonMode mode = TonMode.RELEASE;

    @NonNull
    @Builder.Default
    private String rootDir = "ton/";

    @NonNull
    @Builder.Default
    private String configPath = "{root_dir}config/";

    @NonNull
    @Builder.Default
    private String moduleConfigPath = "{root_dir}config/";

    @NonNull
    @Builder.Default
    private String variableInterpolationSyntax = "{%s}";

    @NonNull
    @Builder.Default
    private String defaultConfigFileExtension = "yaml";

    @Builder.Default
    private String tonEngineID = Constants.tonEngineID;

    @Builder.Default
    private List<String> banner = List.of(
            "",
            "  ██╗  ██╗███████╗██████╗  ██████╗██╗   ██╗██╗     ███████╗███████╗  {blue}┃{reset} A SQL-Editor software using 7149-bernie",
            "  ██║  ██║██╔════╝██╔══██╗██╔════╝██║   ██║██║     ██╔════╝██╔════╝  {blue}┃{reset} Copyright © 2021 - {date_year} Christian Bernstein & contributors",
            "  ███████║█████╗  ██████╔╝██║     ██║   ██║██║     █████╗  ███████╗  {blue}┃{reset}",
            "  ██╔══██║██╔══╝  ██╔══██╗██║     ██║   ██║██║     ██╔══╝  ╚════██║  {blue}┃{reset}   Community: https://discord.gg/ag7G5HPkcF  ",
            "  ██║  ██║███████╗██║  ██║╚██████╗╚██████╔╝███████╗███████╗███████║  {blue}┃{reset}      Source: https://github.com/christian-bernstein/bernie-sql-editor",
            "  ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝╚══════╝  {blue}┃{reset}     License: https://github.com/christian-bernstein/7149-bernie/blob/master/LICENSE",
            "\n"
    );

    @Builder.Default
    private boolean enableBanner = true;

    @Builder.Default
    private String[][] jraPhaseOrder = new String[][]{
            {Constants.threadedJRAPhase},
            {Constants.constructJRAPhase},
            {Constants.useTonJRAPhase},
            {Constants.registerEventClassJRAPhase},
            {Constants.commandClassJRAPhase},
            {Constants.moduleJRAPhase},
            {Constants.configDefinitionJRAPhase},
            {Constants.flowJRAPhase},
            {Constants.cdnJRAPhase},
            {Constants.autoEcexJRAPhase}
    };

    @Builder.Default
    private H2RepositoryConfiguration internalDatabaseConfiguration = H2RepositoryConfiguration.builder()
            .hbm2DDLMode(HBM2DDLMode.UPDATE)
            .databaseDir("./db/")
            .database("ton")
            .username("root")
            .password("root")
            .build();

    @Builder.Default
    private List<LoggerConfig> loggerConfigs = Arrays.asList(
            LoggerConfig.builder().logger("org.java_websocket").type(LoggerSpecificationType.NAME).level(Level.OFF.levelStr).build(),
            LoggerConfig.builder().logger("org.reflections").type(LoggerSpecificationType.NAME).level(Level.OFF.levelStr).build(),
            LoggerConfig.builder().logger("org.hibernate").type(LoggerSpecificationType.NAME).level(Level.OFF.levelStr).build(),
            LoggerConfig.builder().logger("org.jboss").type(LoggerSpecificationType.NAME).level(Level.OFF.levelStr).build()
    );
}
