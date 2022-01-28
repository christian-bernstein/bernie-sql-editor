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

package de.christianbernstein.bernie.shared.db;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Christian Bernstein
 */
@Getter
@Builder
@Accessors(fluent = true)
public class H2RepositoryConfiguration {

    @Singular
    private final List<Class<?>> annotatedClasses;

    @NonNull
    @Getter
    private final String database;

    @NonNull
    @Getter
    private final String databaseDir;

    @NonNull
    @Getter
    private final String username;

    @NonNull
    @Getter
    private final String password;

    @NonNull
    @Builder.Default
    private final HBM2DDLMode hbm2DDLMode = HBM2DDLMode.UPDATE;
}
