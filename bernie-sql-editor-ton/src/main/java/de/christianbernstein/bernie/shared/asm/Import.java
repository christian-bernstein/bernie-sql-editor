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

package de.christianbernstein.bernie.shared.asm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
public class Import implements IImport {

    private final String contract;

    private Class<?> optionalContractClass;

    private final boolean isStatic;

    @NotNull
    @Contract("_, _ -> new")
    public static Import of(@NotNull final Class<?> contractClass, final boolean isStatic) {
        return new Import(contractClass.getName(), contractClass, isStatic);
    }

    @Override
    public String toString() {
        return "import " + (this.isStatic() ? "static " : "") + this.contract() + ";";
    }
}
