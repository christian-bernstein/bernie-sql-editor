/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.modules.user;

import de.christianbernstein.bernie.shared.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import lombok.Builder;
import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
@Builder
public class UserModuleConfig {

    public static UserModuleConfig defaultConfiguration = UserModuleConfig.builder().build();

    @Builder.Default
    private final H2RepositoryConfiguration repositoryConfiguration = H2RepositoryConfiguration.builder()
            .hbm2DDLMode(HBM2DDLMode.UPDATE)
            .databaseDir("./db/")
            .database("ton")
            .username("root")
            .password("root")
            .build();

    @Builder.Default
    private final String rootUsername = "root";

    @Builder.Default
    private final String rootPassword = "root";

}
