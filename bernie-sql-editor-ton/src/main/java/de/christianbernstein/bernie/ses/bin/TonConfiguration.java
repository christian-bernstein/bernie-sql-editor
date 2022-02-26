package de.christianbernstein.bernie.ses.bin;

import de.christianbernstein.bernie.shared.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Standard configuration for Ton.
 * Controls the advanced behavior of the Ton server.
 */
@Getter
@Builder
public class TonConfiguration {

    @NonNull
    private final String workingDirectory;

    @Builder.Default
    private final TonMode mode = TonMode.RELEASE;

    @Builder.Default
    private final String tonEngineID = Constants.tonEngineID;

    @Builder.Default
    private final String[][] jraPhaseOrder = new String[][]{
            {Constants.constructJRAPhase},
            {Constants.useTonJRAPhase},
            {Constants.registerEventClassJRAPhase},
            {Constants.moduleJRAPhase},
            {Constants.flowJRAPhase},
            {Constants.cdnJRAPhase},
            {Constants.autoEcexJRAPhase}
    };

    @Builder.Default
    private final H2RepositoryConfiguration internalDatabaseConfiguration = H2RepositoryConfiguration.builder()
            .hbm2DDLMode(HBM2DDLMode.UPDATE)
            .databaseDir("./db/")
            .database("ton")
            .username("root")
            .password("root")
            .build();
}
