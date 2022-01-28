package de.christianbernstein.bernie.ses.db;

import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseAccessPointLoadConfig {

    @Builder.Default
    private final String username = "root";

    @Builder.Default
    private final String password = "root";

    @Builder.Default
    private final String dialect = "org.hibernate.dialect.H2Dialect";

    @Builder.Default
    private final String driver = "org.h2.Driver";

    @Builder.Default
    private final DBEngineType engineType = DBEngineType.H2;

    @Builder.Default
    private final HBM2DDLMode ddlMode = HBM2DDLMode.UPDATE;
}
