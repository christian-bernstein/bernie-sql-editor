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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Data
@Accessors(fluent = true)
@NoArgsConstructor
public class JavaCode implements IJavaCode<JavaCode> {

    private IImports imports = new Imports();

    private IFormatter formatter = ASMDefaults.defaultGoogleJavaCodeFormatter;

    private String code = "", packageName, nextOpening, nextClosing;

    public JavaCode(@NonNull final String packageName) {
        this.packageName(packageName);
    }

    public JavaCode intelliJCode(@Language("JAVA") @NonNull final String intelliJFormattedJavaCode) {
        this.code(intelliJFormattedJavaCode);
        return this;
    }

    @NotNull
    @Override
    public String compileReady() {
        final StringBuilder sb = new StringBuilder();
        if (this.packageName() != null) {
            sb.append("package ").append(this.packageName()).append(";");
        }
        final String sourcecode = sb.append(this.imports()).append(this.code()).toString();
        return this.formatter() != null ? this.formatter().apply(sourcecode) : sourcecode;
    }

    @Override
    public JavaCode in(@Nullable final String opening, @Nullable final String closing) {
        this.nextOpening(Objects.requireNonNullElse(this.nextOpening(), "") + opening);
        this.nextClosing(Objects.requireNonNullElse(this.nextClosing(), "") + closing);
        return this;
    }

    @Override
    public JavaCode append(@NonNull final String rawCode) {
        if (this.nextOpening() != null) {
            this.code += this.nextOpening();
            this.nextOpening(null);
        }
        this.code += rawCode;
        if (this.nextClosing() != null) {
            this.code += this.nextClosing();
            this.nextClosing(null);
        }
        return this;
    }

    @Override
    public JavaCode append(@NonNull final IJavaCode<?> code, @Nullable final String delimiter) {
        this.imports().addAll(code.imports());
        if (delimiter != null) {
            this.code += delimiter;
        }
        this.append(code.code());
        return this;
    }

    @Override
    public JavaCode appendInTags(@NotNull final String opening, @NotNull final String code, @NotNull final String closing) {
        return this.append(opening + code + closing);
    }
}
