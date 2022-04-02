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

package de.christianbernstein.bernie.sdk.asm;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import de.christianbernstein.bernie.sdk.dynamic.IDynamic;
import de.christianbernstein.bernie.sdk.dynamic.VersionDynamic;
import de.christianbernstein.bernie.sdk.misc.Version;
import de.christianbernstein.bernie.sdk.reflection.JavaReflectiveAnnotationAPI;
import de.christianbernstein.bernie.sdk.tailwind.Bridge;
import de.christianbernstein.bernie.sdk.tailwind.IProteus;
import de.christianbernstein.bernie.sdk.tailwind.IPublicAPI;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@UtilityClass
public final class ASMDefaults {

    public static final String defaultParameterNamingConvention = "param";

    public static final IFormatter defaultGoogleJavaCodeFormatter = code -> {
        try {
            return new Formatter().formatSource(code);
        } catch (final FormatterException e) {
            e.printStackTrace();
            return code;
        }
    };

    public static final IAnnotationToJavaCodeTransformer defaultAnnotationToJavaCodeTransformerV1 = annotation -> {
        final JavaCode code = new JavaCode();
        // Add the annotations import
        code.imports().add(new Import(annotation.annotationType().getName(), false));
        // Retrieve the annotation's values
        @Data
        @Accessors(fluent = true)
        final class Shard {
            private String method, value;

            @Override
            public String toString() {
                return String.format("%s = %s", this.method(), this.value());
            }
        }
        final List<Shard> shards = new ArrayList<>();
        for (final Method shard : annotation.annotationType().getDeclaredMethods()) {
            // Transient annotation methods will be ignored
            if (shard.isAnnotationPresent(JavaReflectiveAnnotationAPI.Transient.class)) {
                continue;
            }
            // Invoke the shard to get its value
            shard.setAccessible(true);
            try {
                final Object value = shard.invoke(annotation);
                shards.add(new Shard().method(shard.getName()).value(
                        (value instanceof String) ?
                                ("\"" + value + "\"") : Objects.toString(value)));
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        // Compile annotation information into Java 8 code
        final String compiledAnnotation = String.format("@%s(%s)",
                annotation.annotationType().getSimpleName(),
                shards.stream().map(Shard::toString).collect(Collectors.joining(", ")));
        code.code(compiledAnnotation);
        return code;
    };

    // TODO: 02.08.2021 Rename with matching naming scheme
    public static final IAnnotationsToJavaCodeTransformer defaultAnnotationToStringFactoryV1 = method -> {
        final IJavaCode<?> code = new JavaCode();
        for (final Annotation annotation : method.getAnnotations()) {
            // Annotations, that are annotated with Transient are ignored, and won't be added to the new contract
            if (annotation.annotationType().isAnnotationPresent(JavaReflectiveAnnotationAPI.Transient.class)) {
                continue;
            }
            final IJavaCode<?> transformedAnnotationCode = defaultAnnotationToJavaCodeTransformerV1.transform(annotation);
            code.append(transformedAnnotationCode).append(" ");
        }
        return code;
    };

    public static final IParameterToJavaCodeTransformer defaultParameterToJavaCodeTransformerV1 = (parameter, index, annotationToJavaCodeTransformer) -> {
        final IJavaCode<?> code = new JavaCode();
        // Append the parameters annotations
        for (final Annotation annotation : parameter.getAnnotations()) {
            code.append(annotationToJavaCodeTransformer.transform(annotation), " ");
        }
        // Append the parameters modifiers
        final int modifiers = parameter.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            code.appendWithSpace("final");
        }
        // Append the parameters type
        final Class<?> type = parameter.getType();
        code.appendWithSpace(type.getSimpleName());
        // Import the parameter type if it isn't a primitive type
        if (!type.isPrimitive()) {
            code.imports().add(new Import(type.getName(), false));
        }

        // Append the parameters name
        code.appendWithCommaSpace(defaultParameterNamingConvention + index);
        return code;
    };

    // TODO: 02.08.2021 check if bride is available
    public static final IBridgeMethodFactory defaultBridgeMethodFactoryV1 = (method, ctx) -> {
        final IJavaCode<?> code = new JavaCode()
                // Add all the method annotations
                .append(defaultAnnotationToStringFactoryV1.transform(method))
                // Add all the method modifiers
                .newLine()
                .appendWithSpace("public")
                // return value
                .appendWithSpace(method.getReturnType().getSimpleName());
        if (!method.getReturnType().isPrimitive()) {
            code.imports().add(new Import(method.getReturnType().getName(), false));
        }
        // Add the method's name
        code.append(method.getName());
        // Add the method's parameters
        if (method.getParameterCount() > 0) {
            // The method has parameters, parse them
            final IJavaCode<?> parameters = new JavaCode();
            int i = 0;
            for (final Parameter parameter : method.getParameters()) {
                parameters.append(defaultParameterToJavaCodeTransformerV1.transform(parameter, i, defaultAnnotationToJavaCodeTransformerV1)).append(", ");
            }
            // TODO: 02.08.2021 Create delimiter function in IJavaCode
            parameters.code(parameters.code().substring(0, parameters.code().length() - 4));
            code.inRoundBrackets().append(parameters);
        } else {
            // Method without parameters
            code.inRoundBrackets().flush();
        }
        // Implement the methods body
        final Bridge bridge = method.getAnnotation(Bridge.class);
        return code.inCurlyBrackets().append(generateGateBridgeMethodBodyV1(method, defaultParameterNamingConvention, bridge));
    };



    public static final IPublicAPIPatternToClassTransformer defaultPublicAPIPatternToClassTransformerV1 = (task) -> {
        final IJavaCode<?> code = task.packageName() != null ? new JavaCode(task.packageName()) : new JavaCode();
        // Generate the class body
        // TODO: 02.08.2021 Better context implementation
        final IASMContext context = new IASMContext() {
        };
        final IJavaCode<?> classBody = new JavaCode();

        classBody.append("private IProteus<?> proteus;").newLine();
        code.imports().add(Import.of(IProteus.class, false));
        code.imports().add(Import.of(IPublicAPI.class, false));
        code.imports().add(Import.of(task.patternClass(), false));
        final JavaCode constructor = new JavaCode().appendWithSpace("public").appendWithSpace(task.implementationClassName()).inRoundBrackets()
                .append("IProteus proteus")
                .inCurlyBrackets().appendWithSemicolon("this.proteus = proteus").newLine();
        classBody.append(constructor);
        final String simpleName = task.patternClass().getSimpleName();
        classBody.append("public final IPublicAPI<" + simpleName + "> proteus(IProteus<" + simpleName + "> proteus) {\nthis.proteus = proteus;\nreturn this;\n}\n");
        classBody.appendWithSpace("public final IProteus<"+ simpleName + ">")
                .append("proteus")
                .inRoundBrackets()
                .flush()
                .inCurlyBrackets()
                .append("\nreturn (IProteus<" + simpleName + ">) this.proteus;\n");

        for (final Method method : task.patternClass().getMethods()) {
            if (method.isAnnotationPresent(Bridge.class)) {
                method.setAccessible(true);
                classBody.append(ASMDefaults.defaultBridgeMethodFactoryV1.create(method, context));
            }
        }
        // Create a compilable Java 8 source of the implementation
        final String publicAPIClassSourceCode = code.appendClassDeclaration(
                task.implementationClassName(),
                "@SuppressWarnings(\"unchecked\") public",
                null,
                List.of(task.patternClass()),
                true,
                classBody).compileReady();

        ASMDefaults.compileAndSaveClass(publicAPIClassSourceCode.getBytes(StandardCharsets.UTF_8), task.classFileLocation() + task.implementationClassName() + ".java");
        final Class<?> publicAPIClass = ASMDefaults.loadClass(task.classFileLocation(), ClassLoader.getSystemClassLoader(), task.packageName(), task.implementationClassName(), true);
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends IPublicAPI<?>> castedPublicAPIClass = (Class<? extends IPublicAPI<?>>) publicAPIClass;
            assert castedPublicAPIClass != null;
            return castedPublicAPIClass;
        } catch (final ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    };

    public static final IDynamic<IPublicAPIPatternToClassTransformer, Version> toClassTransformerVersionDynamic =
            new VersionDynamic<IPublicAPIPatternToClassTransformer>(Version.parseVersion("1.0.0"))
            .add(Version.parseVersion("1.0.0"), defaultPublicAPIPatternToClassTransformerV1);

    @SuppressWarnings("All")
    private static void compileAndSaveClass(@NonNull byte[] bytes, @lombok.NonNull String fileLocation) {
        final File file = new File(fileLocation);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (final FileOutputStream stream = new FileOutputStream(fileLocation)) {
            stream.write(bytes);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, file.getPath());
    }

    @Nullable
    @SuppressWarnings("SameParameterValue")
    private static Class<?> loadClass(@lombok.NonNull String url, @lombok.NonNull ClassLoader parentClassLoader, String packageName, @lombok.NonNull String className, boolean initialize) {
        try {
            final URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(url).toURI().toURL()}, parentClassLoader);
            return Class.forName(className, initialize, classLoader);
        } catch (final ClassNotFoundException | MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    @Contract(pure = true)
    private static String convertPathToPackage(@NonNull final String path) {
        return path.replaceAll("/", ".");
    }

    @NonNull
    public static IJavaCode<?> generateGateBridgeMethodBodyV1(@NonNull final Method method, @NonNull String parameterNamingConvention, @NonNull final Bridge bridge) {
        final IJavaCode<?> code = new JavaCode(), gateCallLambdaCode = new JavaCode();
        // Create gate call lambda code
        final AtomicInteger index = new AtomicInteger(0);
        final String parameterChain = Arrays.stream(method.getParameters())
                .filter(parameter -> !parameter.isAnnotationPresent(JavaReflectiveAnnotationAPI.Transient.class))
                .map(parameter -> parameterNamingConvention + index.getAndIncrement())
                .collect(Collectors.joining(", "));
        // Append the return statement
        if (!method.getReturnType().equals(Void.TYPE)) {
            code.appendReturn();
        }
        // Append the private lambda-gate call
        gateCallLambdaCode.inRoundBrackets().append("ctx, gate").inSpaces().lambdaArrow().appendMethodCall(
                false,
                "ctx.yield",
                true,
                new JavaCode().appendMethodCall(
                        false,
                        "gate." + (bridge.async() ? "async" : "sync"),
                        true,
                        parameterChain
                ).code()
        );
        // Create method body code
        return code.append("this.proteus().internal()").appendMethodCall(
                true,
                bridge.async() ? "async" : "sync",
                false,
                "\"" + bridge.value() + "\"",
                gateCallLambdaCode.code()
        );
    }
}
