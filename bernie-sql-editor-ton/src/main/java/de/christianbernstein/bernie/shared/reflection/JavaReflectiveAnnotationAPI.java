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

package de.christianbernstein.bernie.shared.reflection;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.document.IDocument;
import de.christianbernstein.bernie.shared.misc.*;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * todo add phase detection
 *
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public class JavaReflectiveAnnotationAPI {

    /**
     * Get a set of classes, located in the package list, supplied by the parameter set of strings.
     */
    @FunctionalInterface
    public interface IClassSupplier extends Function<Set<String>, Set<Class<?>>> {
    }

    /**
     * JRP = JavaReflectiveProcessor
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JRP {

        Class<? extends Annotation> type();

        Class<?> at() default Object.class;

        String[] phases() default {};

        /**
         * Defines a field as a pre-conditioner
         *
         * @see IPreconditionChecker
         */
        @Documented
        @Target(ElementType.FIELD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface PreConditioner {
        }
    }

    /**
     * Will be ignored
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Transient {
    }

    /**
     * Perform generic checks, if the class is processable.
     */
    public interface IPreconditionChecker extends BiFunction<ElementType, IDocument<?>, Boolean> {
    }

    /**
     * Contains default implementations of the JRA framework, like class-suppliers, processors etc.
     */
    @UtilityClass
    public class Defaults {

        /**
         * Checks if @Transient is present, and declines handling for that specific member
         * todo get annotation
         *
         * @see Transient
         */
        @JRP.PreConditioner
        public static final IPreconditionChecker transientPreConditioner = (elementType, data) -> {
            final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            if (elementType == ElementType.TYPE) {
                data.<Class<?>>ifPresent("class", aClass -> {
                    if (aClass.isAnnotationPresent(Transient.class)) {
                        atomicBoolean.set(false);
                    }
                });
            } else if (elementType == ElementType.METHOD) {
                data.<Method>ifPresent("method", method -> {
                    if (method.isAnnotationPresent(Transient.class)) {
                        atomicBoolean.set(false);
                    }
                });
            } else if (elementType == ElementType.FIELD) {
                data.<Field>ifPresent("field", field -> {
                    if (field.isAnnotationPresent(Transient.class)) {
                        atomicBoolean.set(false);
                    }
                });
            }
            return atomicBoolean.get();
        };

        /**
         * source: https://stackoverflow.com/a/520344
         */
        public final IClassSupplier internalClassSupplier = new IClassSupplier() {

            /**
             * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
             *
             * @param packageName The base package
             * @return The classes
             */
            private Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                assert classLoader != null;
                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = classLoader.getResources(path);
                List<File> dirs = new ArrayList<>();
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    dirs.add(new File(resource.getFile()));
                }
                ArrayList<Class<?>> classes = new ArrayList<>();
                for (File directory : dirs) {
                    classes.addAll(findClasses(directory, packageName));
                }
                return classes.toArray(new Class[0]);
            }

            /**
             * Recursive method used to find all classes in a given directory and sub-dirs.
             *
             * @param directory   The base directory
             * @param packageName The package name for classes found inside the base directory
             * @return The classes
             */
            private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
                List<Class<?>> classes = new ArrayList<>();
                if (!directory.exists()) {
                    return classes;
                }
                File[] files = directory.listFiles();
                assert files != null;
                for (File file : files) {
                    if (file.isDirectory()) {
                        assert !file.getName().contains(".");
                        classes.addAll(findClasses(file, packageName + "." + file.getName()));
                    } else if (file.getName().endsWith(".class")) {
                        classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                    }
                }
                return classes;
            }

            private Set<Class<?>> getInnerClassesRecursively(@NonNull Class<?> parentClass) {
                Set<Class<?>> innerClasses = new HashSet<>(Arrays.asList(parentClass.getDeclaredClasses()));
                Set<Class<?>> toAdd = new HashSet<>();
                innerClasses.forEach(innerClass -> {
                    toAdd.addAll(getInnerClassesRecursively(innerClass));
                });
                innerClasses.addAll(toAdd);
                return innerClasses;
            }

            @Override
            public Set<Class<?>> apply(Set<String> strings) {
                final Set<Class<?>> collected = new HashSet<>(), innerClasses = new HashSet<>();
                strings.forEach(path -> {
                    try {
                        final Class<?>[] classes = this.getClasses(path);
                        collected.addAll(Arrays.asList(classes));
                    } catch (final ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                });
                collected.forEach(aClass -> {
                    innerClasses.addAll(this.getInnerClassesRecursively(aClass));
                });
                collected.addAll(innerClasses);
                return collected;
            }


        };

        public final IClassSupplier orgReflectionsClassSupplier = (paths) -> {
            final ConfigurationBuilder configuration = new ConfigurationBuilder();
            configuration.setScanners(new SubTypesScanner(false));
            final Set<Collection<URL>> urls = paths.stream().map(ClasspathHelper::forPackage).collect(Collectors.toSet());
            if (urls.isEmpty()) {
                ConsoleLogger.def().log(ConsoleLogger.LogType.WARN, "JRA", "Class supplier 'orgReflectionsClassSupplier' has no specified paths selected, falling back to root package (''). ");
                configuration.addUrls(ClasspathHelper.forPackage(""));
            } else {
                ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "JRA", "Class supplier 'orgReflectionsClassSupplier' will search through packages: " + urls);
            }
            urls.forEach(configuration::addUrls);
            Set<Class<?>> classes = null;
            try {





                // final Reflections reflections = new Reflections(configuration);
                final Reflections reflections = new Reflections(Constants.rootPackage, new SubTypesScanner(false));





                try {
                    classes = reflections.getSubTypesOf(Object.class);
                } catch (final Exception ignored) {
                }
                if (classes == null) {
                    classes = new HashSet<>();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return classes;
        };
    }

    /**
     * Default handler bodies for the different types of locations.
     */
    public static class Processors {

        public interface IProcessor {
        }

        @FunctionalInterface
        public interface IAnnotationAtClassProcessor extends IProcessor {
            void process(@NonNull Annotation annotation, @NonNull Class<?> at, @Nullable IDocument<?> meta, @Nullable Object instance);
        }

        @FunctionalInterface
        public interface IAnnotationAtMethodProcessor extends IProcessor {
            void process(@NonNull Annotation annotation, @NonNull Class<?> at, @NonNull Method method, @Nullable IDocument<?> meta, @Nullable Object instance);
        }

        @FunctionalInterface
        public interface IAnnotationAtFieldProcessor extends IProcessor {
            void process(@NonNull Annotation annotation, @NonNull Class<?> at, @NonNull Field field, @NonNull IDocument<?> meta, @Nullable Object instance);
        }

        @Data
        public static class RegisteredProcessor<T extends IProcessor> {

            private final JRP annotation;

            private final T processor;

            // todo remove -> duplicate in JRP
            private final String[] phases;
        }
    }

    @Builder
    public static class JRA implements Unsafe {

        @NonNull
        private final IClassSupplier classSupplier;

        @Singular
        private final Set<String> paths;
        private final List<Processors.RegisteredProcessor<Processors.IAnnotationAtClassProcessor>> classProcessors = new ArrayList<>();
        private final List<Processors.RegisteredProcessor<Processors.IAnnotationAtMethodProcessor>> methodProcessors = new ArrayList<>();
        private final List<Processors.RegisteredProcessor<Processors.IAnnotationAtFieldProcessor>> fieldProcessors = new ArrayList<>();
        private final List<IPreconditionChecker> preconditionCheckers = new ArrayList<>();

        /**
         * Create cache variable
         * todo introduce {@link ExpiringReference}
         */
        private Set<Class<?>> classCache;

        public JRA init() {
            this._init();
            return this;
        }

        // todo remove method
        @Deprecated
        public void _init() {
            Set<String> localPaths = new HashSet<>(this.paths);
            localPaths.add(this.getClass().getPackageName());
            this.classCache = this.classSupplier.apply(localPaths);
            // Find all the field based processors
            this.classCache.forEach(aClass -> {
                // todo create method
                // Get a possible instance of the current class
                Object instance = null;
                try {
                    for (Field field : aClass.getDeclaredFields()) {
                        if (aClass.equals(field.getType()) && Modifier.isStatic(field.getModifiers()) && aClass.isAnnotationPresent(Instance.class)) {
                            try {
                                instance = field.get(null);
                            } catch (final IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (final Exception e) {
                    // todo handle better
                    e.printStackTrace();
                }
                // Check the types, if they match the processor pattern. If they do so, find and get correct processor type;
                try {
                    for (final Field field : aClass.getDeclaredFields()) {
                        // Check for processors
                        if (field.isAnnotationPresent(JRP.class) && Processors.IProcessor.class.isAssignableFrom(field.getType())) {
                            final JRP jrp = field.getAnnotation(JRP.class);

                            if (!jrp.type().isAnnotationPresent(Retention.class) || !jrp.type().getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME)) {
                                System.err.println("JRA ERROR NOTICE: Having a JRP listening on an annotation which is will be erased at compile-time is probably a bug! (Add or change the annotation-classes retention policy (like: '@Retention(RetentionPolicy.RUNTIME)')). JRP: " + jrp);
                            }

                            try {
                                field.setAccessible(true);
                                if (!Modifier.isStatic(Modifier.fieldModifiers())) {
                                    continue;
                                }
                                // Get the phases of the processor
                                String[] phases = jrp.phases();

                                final Object rawProcessor = field.get(instance);
                                // Get the definitive type of processor
                                final Class<?> rawProcessorClass = rawProcessor.getClass();
                                if (Processors.IAnnotationAtClassProcessor.class.isAssignableFrom(rawProcessorClass)) {
                                    // It's an annotation-at-class processor
                                    this.classProcessors.add(new Processors.RegisteredProcessor<>(jrp, ((Processors.IAnnotationAtClassProcessor) rawProcessor), phases));
                                } else if (Processors.IAnnotationAtMethodProcessor.class.isAssignableFrom(rawProcessorClass)) {
                                    // It's an annotation-at-method processor
                                    this.methodProcessors.add(new Processors.RegisteredProcessor<>(jrp, ((Processors.IAnnotationAtMethodProcessor) rawProcessor), phases));
                                } else if (Processors.IAnnotationAtFieldProcessor.class.isAssignableFrom(rawProcessorClass)) {
                                    // It's an annotation-at-field processor
                                    this.fieldProcessors.add(new Processors.RegisteredProcessor<>(jrp, ((Processors.IAnnotationAtFieldProcessor) rawProcessor), phases));
                                }
                            } catch (final IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        // Check for pre-conditioners
                        if (field.isAnnotationPresent(JRP.PreConditioner.class) && IPreconditionChecker.class.isAssignableFrom(field.getType())) {
                            try {
                                field.setAccessible(true);
                                if (!Modifier.isStatic(Modifier.fieldModifiers())) {
                                    continue;
                                }
                                this.preconditionCheckers.add((IPreconditionChecker) field.get(instance));
                            } catch (final IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (final Exception e){
                    e.printStackTrace();
                }

            });
        }

        private boolean checkProcessorCompatibility(@NonNull JRP jrp, @NonNull Class<?> at, @NonNull Annotation annotation) {
            boolean internal = false;
            if (jrp.at() != Object.class) {
                if (at.equals(jrp.at())) {
                    internal = true;
                }
            }
            if (jrp.type().equals(annotation.annotationType())) {
                internal = true;
            }
            return internal;
        }

        @Nullable
        private Object findInstance(@NonNull Class<?> aClass) {
            Object instance = null;
            for (Field field : aClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Instance.class)) {
                    if (aClass.equals(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                        try {
                            field.setAccessible(true);
                            if (!Modifier.isStatic(Modifier.fieldModifiers())) {
                                continue;
                            }
                            instance = field.get(null);
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return instance;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean checkPreconditions(ElementType elementType, IDocument<?> data) {
            final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            this.preconditionCheckers.forEach(checker -> {
                if (!checker.apply(elementType, data)) {
                    atomicBoolean.set(false);
                }
            });
            return atomicBoolean.get();
        }

        private boolean checkPhaseCompatibility(List<String> phases, Processors.RegisteredProcessor<?> processor) {
            if (phases.isEmpty()) {
                return true;
            }
            final AtomicBoolean ref = new AtomicBoolean(false);
            final List<String> processorPhases = Arrays.asList(processor.getPhases());
            processorPhases.forEach(procPhase -> {
                if (phases.contains(procPhase)) {
                    ref.set(true);
                }
            });
            return ref.get();
        }

        public void process(final IDocument<?> meta, final String... phases) {
            final List<String> listedPhases = new ArrayList<>(Arrays.asList(phases));
            if (this.classCache == null) {
                this._init();
            }
            this.classCache.forEach(aClass -> {
                final Object finalInstance = this.findInstance(aClass);
                // Handle class
                for (final Annotation annotation : aClass.getAnnotations()) {
                    if (!this.checkPreconditions(ElementType.TYPE, Document.of("class", aClass))) break;
                    this.classProcessors.stream().filter(processor -> BoolAccumulator.builder().switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                            .condition(() -> this.checkPhaseCompatibility(listedPhases, processor))
                            .condition(() -> this.checkProcessorCompatibility(processor.getAnnotation(), aClass, annotation)).build().get()).forEach(processor -> {
                        unsafe(() -> processor.getProcessor().process(annotation, aClass, meta, finalInstance));
                    });
                }

                // Handle methods
                final Method[] methods = aClass.getDeclaredMethods();
                for (final Method method : methods) {
                    if (!this.checkPreconditions(ElementType.METHOD, Document.of("method", method))) break;
                    for (final Annotation annotation : method.getAnnotations()) {
                        this.methodProcessors.stream().filter(processor -> BoolAccumulator.builder().switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                                .condition(() -> this.checkPhaseCompatibility(listedPhases, processor))
                                .condition(() -> this.checkProcessorCompatibility(processor.getAnnotation(), aClass, annotation)).build().get()).forEach(processor -> {
                            unsafe(() -> processor.getProcessor().process(annotation, aClass, method, meta, finalInstance));
                        });
                    }
                }

                // Handler fields
                final Field[] fields = aClass.getDeclaredFields();
                for (final Field field : fields) {
                    if (!this.checkPreconditions(ElementType.FIELD, Document.of("field", field))) break;
                    for (final Annotation annotation : field.getAnnotations()) {
                        this.fieldProcessors.stream().filter(processor -> BoolAccumulator.builder().switchType(BoolAccumulator.SwitchType.ONE_WAY_TO_FALSE)
                                .condition(() -> this.checkPhaseCompatibility(listedPhases, processor))
                                .condition(() -> this.checkProcessorCompatibility(processor.getAnnotation(), aClass, annotation)).build().get()).forEach(processor -> {
                            unsafe(() -> processor.getProcessor().process(annotation, aClass, field, meta, finalInstance));
                        });
                    }
                }
            });
        }

        public void process(final String... phases) {
            this.process(Document.empty(), phases);
        }
    }
}
