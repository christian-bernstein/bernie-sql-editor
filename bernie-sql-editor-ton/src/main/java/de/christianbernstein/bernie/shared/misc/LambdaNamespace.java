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

package de.christianbernstein.bernie.shared.misc;

import de.christianbernstein.bernie.shared.document.IDocument;
import lombok.Data;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Bernstein
 */
@Data
public class LambdaNamespace<T> implements ILambdaNamespace<T> {

    private final Map<String, Lambda<T>> lambdas = new HashMap<>();

    private final String namespace;

    public LambdaNamespace(String namespace) {
        this.namespace = namespace;
    }

    public LambdaNamespace() {
        this(LambdaDefinition.GLOBAL_NAMESPACE_IDENTIFIER);
    }

    @NotNull
    @Contract(" -> new")
    public static LambdaNamespace<IDocument<?>> createDefault() {
        return new LambdaNamespace<>();
    }
}
