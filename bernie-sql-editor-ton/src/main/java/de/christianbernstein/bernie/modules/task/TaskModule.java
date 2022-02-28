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

package de.christianbernstein.bernie.modules.task;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Christian Bernstein
 */
public class TaskModule implements ITaskModule {

    private final Map<String, Function<TaskStructFactoryContext, TaskStruct>> structs = Map.of("session-loading-task", context -> TaskStruct.builder()
            .id("session-loading-task")
            .title("Starting SQL-Editor session")
            .piece(PieceStruct.builder()
                    .id("a")
                    .title("a")
                    .build())
            .piece(PieceStruct.builder()
                    .id("b")
                    .title("b")
                    .build())
            .piece(PieceStruct.builder()
                    .id("c")
                    .title("c")
                    .build())
            .piece(PieceStruct.builder()
                    .id("d")
                    .title("d")
                    .build())
            .build()
    );

    @Override
    public TaskStruct getStruct(String id) {
        final Function<TaskStructFactoryContext, TaskStruct> structSupplier = this.structs.get(id);
        if (structSupplier != null) {
            // todo add context
            return structSupplier.apply(null);
        } else return null;
    }
}
