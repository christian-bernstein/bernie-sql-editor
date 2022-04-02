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

package de.christianbernstein.bernie.modules.db;

import de.christianbernstein.bernie.sdk.document.Document;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class DBUtilities {

    @NotNull
    public List<Document> resultSetToList(@NotNull ResultSet set) {
        final List<Document> objs = new ArrayList<>();
        try {
            final String[] cols = new String[set.getMetaData().getColumnCount()];
            for (int i = 0; i < cols.length; i++) {
                cols[i] = set.getMetaData().getColumnName(i + 1);
            }
            while (set.next()) {
                final Document entity = Document.empty();
                for (final String col : cols) {
                    entity.putObject(col, set.getObject(col));
                }
                objs.add(entity);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return objs;
    }
}
