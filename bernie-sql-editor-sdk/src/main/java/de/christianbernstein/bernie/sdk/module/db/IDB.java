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

package de.christianbernstein.bernie.sdk.module.db;

import lombok.NonNull;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * @author Christian Bernstein
 */
public interface IDB {

    Connection getConnection();

    ResultSet query(@NonNull @Language("H2") String statement);

    long update(@NonNull @Language("H2") String statement);

    RequestType determineRequestType(@NonNull @Language("H2") String statement);

}
