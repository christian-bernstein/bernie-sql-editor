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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public interface IJavaCode<Impl extends IJavaCode<Impl>> {

    @NonNull
    String compileReady();

    @NonNull
    IImports imports();

    @NonNull
    String code();

    Impl formatter(@NonNull IFormatter formatter);

    Impl code(@NonNull final String code);

    Impl in(@Nullable final String opening, @Nullable final String closing);

    Impl append(@NonNull final String rawCode);

    Impl append(@NonNull final IJavaCode<?> code, @Nullable final String delimiter);

    default Impl append(@NonNull final IJavaCode<?> code) {
        return this.append(code, null);
    }

    Impl appendInTags(@NonNull final String opening, @NonNull final String code, @NonNull String closing);

    default Impl appendInRoundBrackets(@NonNull final String code) {
        return this.appendInTags("(", code, ")");
    }

    default Impl appendInCurlyBrackets(@NonNull final String code) {
        return this.appendInTags("(", code, ")");
    }

    default Impl appendInSharpBrackets(@NonNull final String code) {
        return this.appendInTags("[", code, "]");
    }

    default Impl appendInDiamondBrackets(@NonNull final String code) {
        return this.appendInTags("<(>", code, ">");
    }

    default Impl appendInSingleQuotationMarksBrackets(@NonNull final String code) {
        return this.appendInTags("'", code, "'");
    }

    default Impl appendInDoubleQuotationMarksBrackets(@NonNull final String code) {
        return this.appendInTags("\"", code, "\"");
    }

    default Impl appendWithSemicolon(@NonNull final String code) {
        return this.appendInTags("", code, ";");
    }

    default Impl appendWithSpace(@NonNull final String code) {
        return this.appendInTags("", code, " ");
    }

    default Impl appendWithCommaSpace(@NonNull final String code) {
        return this.appendInTags("", code, ", ");
    }

    default Impl inRoundBrackets() {
        return this.in("(", ")");
    }

    default Impl inCurlyBrackets() {
        return this.in("{", "}");
    }

    default Impl inSpaces() {
        return this.in(" ", " ");
    }

    default Impl lambdaArrow() {
        return this.append("->");
    }

    // TODO: 02.08.2021 Write Javadoc
    default Impl appendMethodCall(final boolean fluentContinuing, @NonNull final String methodPath, final boolean fluent, Object... parameters) {
        return this.append(String.format("%s%s(%s)%s",
                fluentContinuing ? "." : "",
                methodPath,
                Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(", ")),
                fluent ? "" : ";"));
    }

    // TODO: 02.08.2021 Write Javadoc
    // TODO: 02.08.2021 Check if all the here used classes are imported properly
    default Impl appendClassDeclaration(@NonNull final String name,
                                        @Nullable final String modifiers,
                                        @Nullable final Class<?> superClass,
                                        @Nullable final List<Class<?>> interfaceClasses,
                                        final boolean appendImportsToClassParent,
                                        @Nullable final IJavaCode<?> classBody) {
        if (appendImportsToClassParent && classBody != null) {
            this.imports().addAll(classBody.imports());
        }
        return this.append(String.format("%s class %s %s %s {%s}",
                modifiers,
                name,
                (superClass != null ? "extends " + superClass.getName() : ""),
                (interfaceClasses != null ? "implements " + Objects.requireNonNull(interfaceClasses).stream().map(Class::getSimpleName).collect(Collectors.joining(", ")) : ""),
                (classBody != null ? classBody.code() : "")
        ));
    }

    default Impl appendReturn() {
        return this.appendWithSpace("return");
    }

    default Impl newLine() {
        return this.append("\n");
    }

    default Impl flush() {
        return this.append("");
    }
}
