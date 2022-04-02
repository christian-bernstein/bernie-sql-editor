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

package de.christianbernstein.bernie.sdk.misc;

import de.christianbernstein.bernie.sdk.document.Document;
import de.christianbernstein.bernie.sdk.document.IDocument;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.logging.Level;

/**
 * Point of failure
 * <p>
 * Marks a point in code execution, where a noteworthy edge-case occured.
 */
@Builder
@Data
public class PointOfFailure {

    @Singular
    private final List<Throwable> causes;

    private final String message;

    @Builder.Default
    private final IDocument<?> meta = Document.empty();

    @Builder.Default
    private final Level level = Level.INFO;
}
