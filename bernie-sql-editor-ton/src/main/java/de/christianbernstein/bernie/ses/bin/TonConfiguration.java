package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.shared.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import lombok.*;

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
    private String[][] jraPhaseOrder = new String[][]{
            {Constants.constructJRAPhase},
            {Constants.useTonJRAPhase},
            {Constants.registerEventClassJRAPhase},
            {Constants.moduleJRAPhase},
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
}
