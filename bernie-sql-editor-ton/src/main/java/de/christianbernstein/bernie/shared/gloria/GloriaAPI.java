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

package de.christianbernstein.bernie.shared.gloria;

import com.google.gson.reflect.TypeToken;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.shared.gloria.GloriaAPI.ExecutorAnnotations.Command;
import de.christianbernstein.bernie.shared.misc.Contextual;
import de.christianbernstein.bernie.shared.misc.ICallback;
import de.christianbernstein.bernie.shared.misc.Instance;
import de.christianbernstein.bernie.shared.misc.LambdaNamespace;
import de.christianbernstein.bernie.shared.misc.TriConsumer;
import de.christianbernstein.bernie.shared.misc.Unsafe;
import de.christianbernstein.bernie.shared.module.Module;
import de.christianbernstein.bernie.shared.module.Engine;
import de.christianbernstein.bernie.shared.module.IEngine;
import de.christianbernstein.bernie.shared.module.ModularizedEntityHolder;
import de.christianbernstein.bernie.shared.document.Document;
import de.christianbernstein.bernie.shared.document.Events;
import de.christianbernstein.bernie.shared.document.IDocument;
import de.christianbernstein.bernie.shared.document.IListenerAdapter;
import de.christianbernstein.bernie.shared.event.EventAPI;
import de.christianbernstein.bernie.shared.misc.Tuple.Couple;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.christianbernstein.bernie.shared.module.Events.ModuleRemovedEvent;

/**
 * # STAGE ONE
 * todo use {@link ICallback} instead of weird consumer stuff
 * DONE todo Regex validator annotation -> in addition make the regex string in the annotation @Language("Regex")
 * todo add @DeclarePath to create a path junction without a body
 * DONE todo better argument available handler -> consumer
 * todo add help command in utilities addon
 * todo introduce properties descriptive annotations
 * DONE todo introduce better parameter mapping
 * DONE todo add flow parameters a b c -> []
 * DONE todo add continuous parameters -> a 'b c' d
 * DONE todo add static pre processors
 * DONE todo introduce sessions (requires command sender identification)
 * todo create a thread to execute the commands
 * todo make a queue and let the commands being enqueued instead of called directly
 * todo add more parameter & executor annotations and equivalent validators
 * todo make option to pass certain pre processors
 * todo add command events via callbacks
 * todo introduce global variables and variable parsing
 * todo introduce session variables
 * todo fix method invocation exception when the amount of parameters mismatches the contracts one
 * <p>
 * # STAGE TWO
 * todo remove non-internal imports -> maybe lombok as well
 * todo handle more edge-cases
 * todo use more FunctionalInterfaces to create a more customizable api
 * todo generic way to register/call handlers, that means leaving the method/annotation-based design behind
 * todo move inner classes into their own files to remove some clutter
 * todo write final JDoc and wiki
 *
 * @see Gloria
 *
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
@UtilityClass
public final class GloriaAPI {

    // todo write j doc
    private static final WeakHashMap<String, IGloria> instances = new WeakHashMap<>();

    /**
     * The bare {@link Contextual} has a new function to sync up.
     *
     * @return Synchronized contextual instance
     */
    @Deprecated(forRemoval = true)
    public static Contextual.Sync<String, IGloria> sync() {
        return Contextual.Sync.wrap(() -> {
            return instances;
        });
    }

    // todo write j doc
    public static Contextual<String, IGloria> contextualize() {
        return Contextual.wrap(() -> {
            return instances;
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UtilityClass
    public static final class Utilities {

        // (&)('){} (&)(?=[^'])
        @SuppressWarnings("all")
        public static Pattern findSubstitutionsOfLevel(String idenitifier, String substitutor, int level) {
            return Pattern.compile(String.format("(%s)(%s){%s}", idenitifier, substitutor, level));
        }

        public static String[] splitNonSubstituted(String sequence, String identifier, String substituror) {
            return sequence.split(String.format("(%s)(?=[^%s])", identifier, substituror));
        }

        public static String substitute(String sequence, String identifier, String substitutor) {
            final AtomicReference<String> atomicSequence = new AtomicReference<>(sequence);
            Pattern.compile(String.format("(%s)(%s)+", identifier, substitutor)).matcher(sequence).results().forEach(result -> {
                final String group = result.group();
                atomicSequence.set(atomicSequence.get().replace(group, group.substring(0, group.length() - 1)));
            });
            return atomicSequence.get();
        }

        /**
         * A printable node is a way to print nodes in the linux tree command style
         *
         * @author VasiliNovikov - https://stackoverflow.com/a/8948691
         */
        @Data
        @Builder
        public static class PrintableNode {

            @NonNull
            final String text;

            @Singular
            final List<PrintableNode> children;

            public PrintableNode addRawChild(String text) {
                this.children.add(new PrintableNode(text, new ArrayList<>()));
                return this;
            }

            public String toString() {
                StringBuilder buffer = new StringBuilder(200);
                print(buffer, "", "");
                return buffer.toString();
            }

            public void renderAndHandle(@NonNull Consumer<String> lineHandler) {
                for (String line : this.toString().split("\\n")) {
                    lineHandler.accept(line);
                }
            }

            private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
                buffer.append(prefix);
                buffer.append(text);
                buffer.append('\n');
                for (Iterator<PrintableNode> it = children.iterator(); it.hasNext(); ) {
                    PrintableNode next = it.next();
                    if (it.hasNext()) {
                        next.print(buffer, childrenPrefix + "├─", childrenPrefix + "│ ");
                    } else {
                        // todo use ╰ └
                        next.print(buffer, childrenPrefix + "╰─", childrenPrefix + "  ");
                    }
                }
            }

            public static PrintableNode fromText(String text) {
                return new PrintableNode(text, new ArrayList<>());
            }

            public static PrintableNode fromKeyValueText(String key, Object value, int lat) {
                return fromKeyValueText(key, value, lat, lat);
            }

            public static PrintableNode fromKeyValueText(String key, Object value, int latKey, int latVal) {
                return new PrintableNode(String.format("%" + latKey + "s : %-" + latVal + "s", key, value.toString()), new ArrayList<>());
            }
        }
    }

    @NotNull
    @Contract("_, _ -> param1")
    @SuppressWarnings("UnusedReturnValue")
    public static <T> Stream<T> forEachIndexed(@NotNull @NonNull Stream<T> stream, @NonNull IStreamTraversConsumer<T> consumer) {
        final AtomicInteger index = new AtomicInteger(0);
        final TraversalContext context = new TraversalContext();
        final Iterator<T> iterator = stream.iterator();
        while (iterator.hasNext()) {
            final T next = iterator.next();
            consumer.accept(context, index.getAndIncrement(), next);
            if (context.isBreakAfterThis()) {
                break;
            }
        }
        return stream;
    }

    public static IDocument<?> serializeAnnotations(@NonNull Annotation annotation) {
        return _serializeAnnotation(annotation, new Document());
    }

    // todo serialize annotation container methods
    @Contract("_, _ -> param2")
    private static IDocument<?> _serializeAnnotation(@NotNull @NonNull Annotation recAnnotation, IDocument<?> recDocument) {
        // Serialize the annotations values
        Arrays.asList(recAnnotation.annotationType().getDeclaredMethods()).forEach(method -> {
            try {
                recDocument.putObject(method.getName(), method.invoke(recAnnotation));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        // Serialize all the annotation's annotations
        for (final Annotation subAnnotation : recAnnotation.annotationType().getAnnotations()) {
            final Class<? extends Annotation> annotationType = subAnnotation.annotationType();
            if (annotationType.equals(Documented.class) || annotationType.equals(Retention.class) || annotationType.equals(Target.class)) {
                continue;
            }
            final IDocument<?> subDocument = _serializeAnnotation(subAnnotation, new Document());
            recDocument.putObject("_" + annotationType.getName(), subDocument);
        }
        return recDocument;
    }

    public enum APIType {

        METHOD("method"), FLUENT("fluent");

        @Getter
        final String type;

        APIType(@NonNull String type) {
            this.type = type;
        }
    }

    public interface IStreamTraversConsumer<T> extends TriConsumer<TraversalContext, Integer, T> {

        @SuppressWarnings("unused")
        @NotNull
        @Contract(pure = true)
        static <T> IStreamTraversConsumer<T> wrap(@NonNull Consumer<T> consumer) {
            return (v1, v2, v3) -> consumer.accept(v3);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // STREAMLINED API todo implement functionality
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FunctionalInterface
    public interface StreamlinedCommandHandler extends Function<Statement, CommandResult> {

        static StreamlinedCommandHandler fromConsumer(@NonNull Consumer<Statement> handler) {
            return new StreamlinedCommandHandler() {
                @Override
                public CommandResult apply(Statement statement) {
                    handler.accept(statement);
                    // todo better result;
                    return new CommandResult(statement);
                }
            };
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MODERNIZED INTRINSIC API
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * When a command is submitted, it might contain raw data, like internal variables, simple calculations or even more
     * complex java code segments. The lecturer replaces them with their actual values.
     * <p>
     * E.g $(10.4 * 1.74) -> 18.096D, $name -> Imke
     */
    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface ILecturer {

        String lecture(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData, String inboundHandler);

    }

    @FunctionalInterface
    public interface IInboundHandler {

        // todo remove BiConsumer -> feature replacement
        @SuppressWarnings("UnusedReturnValue")
        CommandResult handle(@NonNull IGloria gloria, @NonNull String command, @NonNull ICommandSender sender, @NonNull IDocument<?> unorderedData, BiConsumer<Statement, CommandResult> callback);

    }

    @FunctionalInterface
    public interface IStatementProcessor extends Unsafe {

        void handle(@NonNull Statement statement);

    }

    @FunctionalInterface
    public interface IValidator {

        boolean test(@NonNull SerializedAnnotation annotation, @NonNull SerializedParameter parameter, Object object);

    }

    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface IParameterSupplier {

        Object get(@NonNull Statement statement, @NonNull SerializedExecutor executor, @NonNull SerializedParameter parameter, int position);

    }

    @FunctionalInterface
    public interface ITranslator<T> extends BiFunction<SerializedParameter, String, T> {

        static <T> ITranslator<T> fromString(Function<String, T> fun) {
            return (serializedParameter, s) -> fun.apply(s);
        }

        static <T extends Enum<T>> ITranslator<T> createEnumTranslator(final Class<T> type) {
            return (serializedParameter, s) -> {
                try {
                    return Enum.valueOf(type, s);
                } catch (final IllegalArgumentException e) {
                    return Enum.valueOf(type, s.toUpperCase(Locale.ROOT));
                }
            };
        }
    }

    public interface IGloria extends Unsafe {

        /**
         * Used to retrieve a {@link PrintStream}, that is passed through the commands unordered data to the handler.
         */
        String CUSTOM_COMMAND_PRINT_STREAM_IDENTIFIER = "output_stream";

        /**
         * todo implement the usage of this variable
         * Selects the default inbound handler, that is provided by the base module
         */
        String DEFAULT_INBOUND_HANDLER_IDENTIFIER = "sync";

        String getInstanceIdentifier();

        IEngine<IGloria> getModuleEngine();

        ISessionManager getSessionManager();

        INode getCommandNode(String path);

        List<INode> getCommandNodes();

        Queue<Statement> getAsyncQueue();

        INode getMostMatchingNode(String path);

        IDocument<?> getConfiguration();

        IGloria setDefaultCommandPrintStream(@NonNull PrintStream defaultPrintStream);

        @Nullable
        PrintStream getDefaultCommandPrintStream();

        CompletableFuture<CommandResult> submitAsync(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData);

        @SuppressWarnings("UnusedReturnValue")
        CommandResult submit(@NonNull String command);

        @SuppressWarnings("UnusedReturnValue")
        CommandResult submit(@NonNull String command, String inboundHandler);

        @SuppressWarnings("UnusedReturnValue")
        CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, String inboundHandler);

        @SuppressWarnings("UnusedReturnValue")
        CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData, String inboundHandler);

        @SuppressWarnings("UnusedReturnValue")
        CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData, String inboundHandler, BiConsumer<Statement, CommandResult> callback);

        @SuppressWarnings("UnusedReturnValue")
        IGloria registerMethodHandler(Method method, @Nullable Object instance);

        @SuppressWarnings("UnusedReturnValue")
        IGloria registerMethodsInClass(@NonNull Class<?> holder, boolean autoInstanceInvoking);

        Statement deserialize(@NonNull IGloria api, @NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData);

        boolean setDefaultInboundHandler(@NonNull String identifier);

        void removeNodeIf(@NonNull Predicate<INode> predicate);

        void removeNode(@NonNull String node);

        void start();

        void shutdown();

    }

    @SuppressWarnings("unused")
    public interface INode {

        String getId();

        List<String> getAliases();

        List<INode> getChildren();

        boolean matches(String shard);

        boolean matches(INode that);

        boolean isSkeleton();

        RegisteredCommandNode getCommandBody();

        // todo write j doc
        void transformInto(@NonNull INode node);

        Utilities.PrintableNode toPrintableNode();

        String toString();

    }

    @SuppressWarnings("unused")
    public interface ICommandSender {

        /**
         * A de.christianbernstein.bernie.ses.ton.user granted with all permissions
         */
        ICommandSender root = new CommandSender(UUID.randomUUID(), new LambdaNamespace<>(), Collections.emptyList(), "root", Collections.singletonList("root")) {
            @Override
            public List<String> checkPermissions(String... permissions) {
                return Collections.emptyList();
            }
        };

        /**
         * A default de.christianbernstein.bernie.ses.ton.user with no permissions set
         */
        ICommandSender def = new CommandSender(UUID.randomUUID(), new LambdaNamespace<>(), Collections.emptyList(), "default", Arrays.asList("fallback", "default")) {
            @Override
            public List<String> checkPermissions(String... permissions) {
                return Arrays.asList(permissions);
            }
        };

        UUID getUniqueID();

        void sendMessage(String... messages);

        boolean hasPermissions(String... permissions);

        List<String> checkPermissions(String... permissions);

        List<String> getPermissions();

        LambdaNamespace<Document> getExposedAPI();

        String getName();

        List<String> getTags();

        boolean isTaggedWith(String... tags);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEFAULTS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UtilityClass
    public static class Defaults {

        @UtilityClass
        public static class DefaultTranslators {

            public static final IdentifiedTranslator<TimeUnit> TIMEUNIT_TRANSLATOR = new IdentifiedTranslator<>(TimeUnit.class, ITranslator.createEnumTranslator(TimeUnit.class));

        }
    }

    // todo method to register sessions
    // todo create a not closable session (handle in expiration policy as well)
    @SuppressWarnings("unused")
    public interface ISession {

        UUID getIdentifier();

        String getSessionName();

        @Nullable
        Instant getExpiringTime();

        Instant getSessionStart();

        IDocument<?> getSessionData();

        @SuppressWarnings("UnusedReturnValue")
        ISession setSessionData(IDocument<?> newSessionData);

        void onSessionClose();

    }

    @SuppressWarnings("unused")
    public interface ISessionManager {

        ISession STATIC_SESSION = new Session(UUID.randomUUID(), "_static_session", Instant.now(), null);

        ISession getStaticSession();

        Optional<ISession> getSession(@NonNull UUID identifier, @NonNull String sessionName);

        ISession createSession(@NonNull UUID identifier, @NonNull String sessionName);

        ISession createExpiringSession(@NonNull UUID identifier, @NonNull String sessionName, Instant expirationTime);

        void closeSession(@NonNull UUID identifier, @NonNull String sessionName);

        void closeSessionsOf(@NonNull UUID identifier);

        List<ISession> getSessionsOf(@NonNull UUID identifier);

        List<ISession> getSessionsWithName(@NonNull String sessionName);

        boolean containsSession(@NonNull UUID identifier, @NonNull String sessionName);

        int countSessionsOf(@NonNull UUID identifier);

        void shutdown();
    }

    // todo rename
    @Data
    public static class TraversalContext {

        private boolean breakAfterThis = false;

    }

    // todo implement
    @Data
    public static class CommandResult {

        private final Statement statement;

    }

    @Data
    public static class SerializedAnnotation {

        private final String annotationName;

        private final Class<? extends Annotation> annotationType;

        private final APIType type;

        private final IDocument<?> serializedData;

        private final IDocument<?> unorderedData;

        public static SerializedAnnotation serialize(@NonNull Annotation annotation) {
            final IDocument<?> serializedData = serializeAnnotations(annotation), unorderedData = Document.of("annotation", annotation, "class", annotation.annotationType());
            return new SerializedAnnotation(annotation.annotationType().getName(), annotation.annotationType(), APIType.METHOD, serializedData, unorderedData);
        }
    }

    @Data
    public static class SerializedParameter {

        private final String parameterName;

        private final APIType type;

        private final boolean intrinsic;

        private final List<SerializedAnnotation> annotations;

        private final Class<?> parameterType;

        private final IDocument<?> unorderedData;

        @Nullable
        private final SerializedAnnotation parameterAnnotation;

        @NotNull
        @Contract("_ -> new")
        public static SerializedParameter serialize(@NonNull Parameter parameter) {
            final List<SerializedAnnotation> annotations = new ArrayList<>();
            for (final Annotation annotation : parameter.getAnnotations()) {
                annotations.add(SerializedAnnotation.serialize(annotation));
            }
            final Optional<SerializedAnnotation> parameterAnnotationOptional = annotations.stream().filter(serializedAnnotation -> serializedAnnotation.getAnnotationType().equals(ParamAnnotations.Param.class)).findFirst();
            parameterAnnotationOptional.ifPresent(annotations::remove);
            final boolean intrinsic = annotations.stream().anyMatch(serializedAnnotation -> {
                return serializedAnnotation.getSerializedData().containsKey("_" + IntrinsicParameterAnnotations.APIIntrinsicAnnotation.class.getName());
            });
            return new SerializedParameter(parameter.getName(), APIType.METHOD, intrinsic, annotations, parameter.getType(), Document.empty(), parameterAnnotationOptional.orElse(null));
        }

        public Optional<SerializedAnnotation> getAnnotation(@NonNull Class<? extends Annotation> annotation) {
            return this.annotations.stream().filter(serializedAnnotation -> {
                if (serializedAnnotation.getType() == APIType.METHOD) {
                    final Class<? extends Annotation> annotationClass = serializedAnnotation.getUnorderedData().get("class");
                    return annotationClass.equals(annotation);
                } else if (serializedAnnotation.getType() == APIType.FLUENT) {
                    return serializedAnnotation.getAnnotationName().equals(annotation.getName());
                } else {
                    return false;
                }
            }).findFirst();
        }

        public boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotation) {
            return this.getAnnotation(annotation).isPresent();
        }

        public SerializedAnnotation getAnnotationOptimistically(@NonNull Class<? extends Annotation> annotation) {
            return this.getAnnotation(annotation).orElse(null);
        }

        @SuppressWarnings("unused")
        public SerializedParameter ifAnnotationPresent(@NonNull Class<? extends Annotation> annotation, @NonNull Consumer<SerializedParameter> handler) {
            if (this.isAnnotationPresent(annotation)) {
                handler.accept(this);
            }
            return this;
        }

        // todo remove?
        @Override
        public String toString() {
            return "SerializedParameter{" +
                    "parameterName='" + parameterName + '\'' +
                    ", type=" + type +
                    ", intrinsic=" + intrinsic +
                    ", annotations=" + annotations +
                    ", parameterType=" + parameterType +
                    ", unorderedData=" + unorderedData +
                    ", parameterAnnotation=" + parameterAnnotation +
                    '}';
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPRECATED INTRINSIC API
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Data
    public static class SerializedExecutor {

        private final String type;

        private final String name;

        private final IDocument<?> unorderedData;

        private final List<SerializedAnnotation> executorAnnotations;

        private final List<SerializedParameter> parameters;
    }

    @Data
    public abstract static class IntrinsicParameterSupplier {

        @NonNull
        private final Class<? extends Annotation> intrinsicAnnotationTarget;

        protected abstract Object get(@NonNull SerializedAnnotation serializedIntrinsicAnnotation, @NonNull Statement statement, @SuppressWarnings("SameParameterValue") SerializedExecutor executor,
                                      @NonNull SerializedParameter parameter, int position);

    }

    @Data
    public abstract static class IdentifiedInboundHandler implements IInboundHandler {

        @NonNull
        private final String type;

        // todo better way to determining the ability of handling chained commands
        private final boolean supportsCommandChaining;
    }

    @UtilityClass
    public class ParamValidators {

        public static final ParamValidator rangeValidator = new ParamValidator(ParamAnnotations.Range.class, (data, parameter, object) -> {
            double v, min = data.getSerializedData().getDouble("min"), max = data.getSerializedData().getDouble("max");
            try {
                v = Double.parseDouble(object.toString());
            } catch (final Exception e) {
                e.printStackTrace();
                return false;
            }
            return v >= min && v <= max;
        });

        public static final ParamValidator regexValidator = new ParamValidator(ParamAnnotations.Regex.class, (data, parameter, object) -> {
            String regex = data.getSerializedData().getString("value"), toCheck = object.toString();
            return Pattern.matches(regex, toCheck);
        });

        public static final ParamValidator flowValidator = new ParamValidator(ParamAnnotations.Flow.class, (data, parameter, object) -> {
            if (parameter.getParameterType().isArray()) {
                try {
                    final String[] array = ((String[]) object);
                    if (array == null) {
                        return data.getSerializedData().getLong("minLatitude") <= 0;
                    } else {
                        return array.length <= data.getSerializedData().getLong("maxLatitude") && array.length >= data.getSerializedData().getLong("minLatitude");
                    }
                } catch (final ClassCastException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                String[] array;
                if (object == null) {
                    array = new String[0];
                } else {
                    array = object.toString().split("( )+");
                }
                return array.length <= data.getSerializedData().getLong("maxLatitude") && array.length >= data.getSerializedData().getLong("minLatitude");
            }
        });

        public static final ParamValidator relayValidator = new ParamValidator(ParamAnnotations.Relay.class, (data, parameter, object) -> {
            final Class<? extends IValidator> validator = data.getSerializedData().get("value");
            final Constructor<? extends IValidator> constructor;
            try {
                constructor = validator.getConstructor();
                constructor.setAccessible(true);
                final IValidator instance = constructor.newInstance();
                return instance.test(data, parameter, object);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        });

        public static final ParamValidator paramValidator = new ParamValidator(ParamAnnotations.Param.class, (data, parameter, object) -> {
            if (data.getSerializedData().getBool("mandatory") && object == null) {
                System.out.println("A parameter is null but shouldn't be");
                return false;
            }
            return true;
        });
    }

    @UtilityClass
    public class ParamAnnotations {

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Range {

            double min() default Double.MIN_VALUE;

            double max() default Double.MAX_VALUE;

        }

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Regex {

            @Language("RegExp")
            String value();

        }

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Param {

            String name() default "";

            boolean mandatory() default true;

            String description() default "";
        }

        // todo assess is the param (plain string should be null or empty string)
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Flow {

            long minLatitude() default 0L;

            long maxLatitude() default Long.MAX_VALUE;

            boolean trimAfterMaxLatitude() default false;

        }

        /**
         * Let's the system check the parameter with a local validator
         */
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface Relay {

            Class<? extends IValidator> value();

        }

        // todo write jdoc
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface DurationMeta {

            TimeUnit value() default TimeUnit.SECONDS;

        }
    }

    @UtilityClass
    public class PreProcessors {

        public static final StatementPreProcessor deactivatedPreProcessors = new StatementPreProcessor(ExecutorAnnotations.Deactivated.class, "deactivated_pre_processor", statement -> statement.setCanceled(true));

        public static final StatementPreProcessor authorizationPreProcessors = new StatementPreProcessor(ExecutorAnnotations.Authorization.class, "authorization_pre_processor", statement -> {
            final ICommandSender sender = statement.getSender();
            final ExecutorAnnotations.Authorization authorization = statement.getNode().getHandler().getAnnotation(ExecutorAnnotations.Authorization.class);
            final List<String> missingPermissions = sender.checkPermissions(authorization.permissions());
        });
    }

    @UtilityClass
    public class ExecutorAnnotations {

        /**
         * Bundle commands together. This is used to create short-hands for command handlers
         */
        @Target({ElementType.METHOD, ElementType.FIELD})
        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        public @interface PolymorphCommand {

            Command[] value();

        }

        /**
         * @see PolymorphCommand Can bundle many command nodes to a single handler.
         */
        @Target({ElementType.METHOD, ElementType.FIELD})
        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        @Repeatable(PolymorphCommand.class)
        public @interface Command {

            @SuppressWarnings("unused")
            String EMITTER_TAG = "emitter";

            String GLOBAL_COMMAND_MODULE = "global";

            String path() default "";

            String literal();

            String[] aliases() default {};

            String description() default "";

            String sampleUsage() default "";

            String[] passPreprocessors() default {};

            String ofModule() default GLOBAL_COMMAND_MODULE;

            /**
             * @return An array of strings to categorize the command handler
             */
            String[] tags() default {};

            Arguments arguments() default @Arguments;

            /**
             * todo change to an enum
             *
             * @return A boolean determining the type of command
             */
            Type type() default Type.UNDEFINED;

            enum Type {

                HANDLER, JUNCTION, UNDEFINED

            }

            @Documented
            @Retention(RetentionPolicy.RUNTIME)
            @interface Arguments {

                Argument[] value() default {};

            }

            @Documented
            @Repeatable(Arguments.class)
            @Retention(RetentionPolicy.RUNTIME)
            @interface Argument {

                String identifier();

                String[] aliasses() default {};

                String description() default "";

            }

            // todo check functionality and maybe refactor odd parts
            @Accessors(chain = true, fluent = true) // todo what does this do?
            @Builder
            class Streamlined implements Command {

                private final String path, literal, description, sampleUsage, ofModule;

                private final String[] aliases, tags, passPreprocessors;

                // todo introduce streamlined form
                private final Arguments arguments;

                private final Type type;

                @Override
                public String path() {
                    return this.path;
                }

                @Override
                public String literal() {
                    return this.literal;
                }

                @Override
                public String[] aliases() {
                    return this.aliases;
                }

                @Override
                public String description() {
                    return this.description;
                }

                @Override
                public String sampleUsage() {
                    return this.sampleUsage;
                }

                @Override
                public String[] passPreprocessors() {
                    return this.passPreprocessors;
                }

                @Override
                public String ofModule() {
                    return this.ofModule;
                }

                @Override
                public String[] tags() {
                    return this.tags;
                }

                @Override
                public Arguments arguments() {
                    return this.arguments;
                }

                @Override
                public Type type() {
                    return this.type;
                }

                /**
                 * Returns the annotation type of this annotation.
                 *
                 * @return the annotation type of this annotation
                 */
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Command.class;
                }
            }
        }

        /**
         * An annotation, annotated with @PreProcessorHook will be passed to the pre processors.
         * If a preprocessor matches the annotated annotation, it will be called
         */
        @Target(ElementType.ANNOTATION_TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface PreProcessorHook {
        }

        /**
         * @see PreProcessorHook Parent annotation
         */
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @PreProcessorHook
        @Documented
        public @interface Deactivated {
        }

        /**
         * @see PreProcessorHook Parent annotation
         */
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @PreProcessorHook
        @Documented
        public @interface Authorization {

            String[] permissions() default {};

        }
    }

    @UtilityClass
    public class IntrinsicParameterAnnotations {

        /**
         * Defines an intrinsic annotation
         */
        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        public @interface APIIntrinsicAnnotation {
        }

        /**
         * todo test
         * Get the current instance of IGloria
         */
        @Documented
        @APIIntrinsicAnnotation
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface API {
        }

        @Documented
        @APIIntrinsicAnnotation
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface APISession {

            String STATIC_SESSION = "_static_session";

            String value() default STATIC_SESSION;

            boolean autoCreate() default true;
        }

        @Documented
        @APIIntrinsicAnnotation
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface APIStatement {
        }
    }

    @UtilityClass
    public class DefaultMappers {

        public static final Mapper defaultMapper = new Mapper("default") {

            @Override
            protected @NonNull Object[] map(@NonNull IGloria api, @NonNull Object[] parameters, @NonNull Statement statement) {
                int globalIndex = 0, commandParameterIndex = 0;
                for (final SerializedParameter parameter : statement.getNode().getParameters()) {
                    // If an element is already defined, skip that parameter
                    // todo investigate: Condition 'parameters[globalIndex] != null' is always 'true'
                    //noinspection ConstantConditions
                    if (parameters[globalIndex] != null) {
                        continue;
                    }
                    final AtomicReference<Object> resolvedParameter = new AtomicReference<>();
                    // Map the parameter
                    mapper:
                    if (parameter.isIntrinsic()) {
                        // Use internal suppliers to generate the parameters
                        // This system will only support one api-intrinsic annotation per parameter
                        final AtomicReference<IntrinsicParameterSupplier> supplierReference = new AtomicReference<>();
                        final Optional<SerializedAnnotation> serializedAnnotationOptional = parameter.getAnnotations().stream().filter(annotation -> {
                            final IDocument<?> document = annotation.getSerializedData().get("_" + IntrinsicParameterAnnotations.APIIntrinsicAnnotation.class.getName());
                            return document != null;
                        }).findFirst();
                        int finalGlobalIndex = globalIndex;
                        serializedAnnotationOptional.ifPresent(serializedAnnotation -> {
                            api.getModuleEngine().<IntrinsicParameterSupplier>getEntityHolder("intrinsic_suppliers").flatMap(holder -> holder.normalize().stream().filter(supplier -> {
                                return supplier.getIntrinsicAnnotationTarget().equals(serializedAnnotation.getAnnotationType());
                            }).findFirst()).ifPresent(supplierReference::set);
                            // todo insert executor object
                            resolvedParameter.set(supplierReference.get().get(serializedAnnotationOptional.get(), statement, null, parameter, finalGlobalIndex));
                        });
                    } else {
                        // The parameter comes from the command itself
                        if (parameter.isAnnotationPresent(ParamAnnotations.Flow.class)) {
                            // The flow parameter maps all the left over parts in the commands supplied data to a single parameter
                            // The parameter can be a String or an generic array
                            final SerializedAnnotation flow = parameter.getAnnotationOptimistically(ParamAnnotations.Flow.class);
                            List<Object> flowingRawValues = new ArrayList<>();
                            do {
                                flowingRawValues.add(statement.getParameters().getString(String.valueOf(commandParameterIndex)));
                                commandParameterIndex++;
                            } while (statement.getParameters().containsKey(String.valueOf(commandParameterIndex)));
                            // Trim the values if necessary
                            if (flow.getSerializedData().getBool("trimAfterMaxLatitude")) {
                                final int maxLatitude = flow.getSerializedData().getInt("maxLatitude");
                                if (flowingRawValues.size() > maxLatitude) {
                                    flowingRawValues.subList(maxLatitude, flowingRawValues.size()).clear();
                                }
                            }
                            // Map the values onto the parameter array -> String[]
                            if (parameter.getParameterType().isArray()) {
                                if (parameter.getParameterType().getComponentType().equals(String.class)) {
                                    resolvedParameter.set(flowingRawValues.toArray(new Object[0]));
                                } else {
                                    System.out.println("the parameter isn't a string, potentially fatal: " + parameter.getParameterType().getTypeName());
                                }
                            } else {
                                if (parameter.getParameterType().equals(String.class)) {
                                    // The flow parameter is a simple String (.join(" "))
                                    resolvedParameter.set(flowingRawValues.stream().map(Object::toString).collect(Collectors.joining(" ")));
                                } else if (new TypeToken<List<CharSequence>>() {
                                }.getRawType().equals(parameter.getParameterType())) {
                                    // The flow parameter is a List<String> or List<CharSequence>
                                    resolvedParameter.set(new ArrayList<>(flowingRawValues));
                                } else {
                                    // There is something odd going on, it's not possible to parse it
                                    System.out.println("the parameter isn't a string, potentially fatal: " + parameter.getParameterType().getTypeName());
                                }
                            }
                            break mapper;
                        } else {
                            // Its a simple parameter, add and continue
                            final String query = String.valueOf(commandParameterIndex);
                            if (statement.getParameters().containsKey(query)) {
                                final Object rawValue = statement.getParameters().get(query);
                                // Translate the string input to the parameters requested type via the modularized translator holder
                                final Class<?> parameterType = parameter.getParameterType();
                                final AtomicReference<Object> cast = new AtomicReference<>();
                                final AtomicBoolean castSuccessful = new AtomicBoolean(false);
                                api.getModuleEngine().<IdentifiedTranslator<?>>getEntityHolder("translators").ifPresent(holder -> {
                                    final Optional<IdentifiedTranslator<?>> translator = holder.normalize().stream().filter(identifiedTranslator -> identifiedTranslator.getIdentifier().equals(parameterType)).findFirst();
                                    if (translator.isPresent()) {
                                        try {
                                            cast.set(translator.get().getTranslator().apply(parameter, rawValue.toString()));
                                            castSuccessful.set(true);
                                        } catch (final Exception e) {
                                            e.printStackTrace();
                                            System.out.println("translator failed, abort execution");
                                        }
                                    }
                                });
                                if (!castSuccessful.get()) {
                                    try {
                                        cast.set(parameterType.cast(rawValue));
                                    } catch (final ClassCastException ignored) {
                                    }
                                }
                                resolvedParameter.set(cast.get());
                            }
                            commandParameterIndex++;
                        }
                    }
                    parameters[globalIndex] = resolvedParameter.get();
                    globalIndex++;
                }
                return parameters;
            }
        };
    }

    @Data
    public abstract static class Mapper {

        @NonNull
        private final String name;

        @NonNull
        protected abstract Object[] map(@NonNull IGloria api, @NonNull Object[] parameters, @NonNull Statement statement);
    }

    /**
     * todo parse inbound handler
     */
    @Data
    public static class Statement {

        private final IGloria api;

        private final String raw;

        private final ICommandSender sender;

        private final IDocument<?> unorderedData;

        private final IDocument<?> parameters;

        private final IDocument<?> arguments;

        private final RegisteredCommandNode node;

        /**
         * todo make more advances logging system using java loggers and/or bernie's logging framework
         * this is a way to redirect console output of an command
         */
        private final PrintStream out;

        private final List<String> emissions = new ArrayList<>();

        // todo implement
        private final IDocument<?> advancedEmissions = Document.empty();

        private boolean canceled = false;

        @SuppressWarnings("UnusedReturnValue")
        @NonNull
        public Statement emit(final String emission) {
            this.emissions.add(emission);
            return this;
        }
    }

    @Data
    public static class RegisteredCommandNode {

        private final IStatementProcessor processor;

        private final Command command;

        // todo change to SerializedAnnotations
        private final List<Annotation> executorAnnotations;

        private final Method handler;

        private final Object handlerInstance;

        private final List<SerializedParameter> parameters;
    }

    @Data
    public static class ParamValidator {

        private final Class<? extends Annotation> target;

        private final IValidator validator;
    }

    @Data
    public static class StatementPreProcessor {

        private final Class<? extends Annotation> annotation;

        private final String id;

        private final IStatementProcessor processor;
    }

    @Data
    public static class CommandSender implements ICommandSender {

        private final UUID uniqueID;

        private final LambdaNamespace<Document> exposedAPI;

        private final List<String> permissions;

        private final String name;

        private final List<String> tags;

        @Override
        public void sendMessage(String... messages) {
            for (final String message : messages) {
                // todo change to a print stream
                System.out.println(message);
            }
        }

        @Override
        public boolean hasPermissions(String... permissions) {
            return this.checkPermissions(permissions).isEmpty();
        }

        @Override
        public List<String> checkPermissions(String... permissions) {
            final List<String> missingPermissions = new ArrayList<>();
            for (final String permission : permissions) {
                if (!this.getPermissions().contains(permission)) {
                    missingPermissions.add(permission);
                }
            }
            return missingPermissions;
        }

        // todo test method
        @Override
        public boolean isTaggedWith(String... tags) {
            if (tags == null || tags.length == 0) {
                return true;
            }
            return Stream.of(tags).filter(tag -> this.getTags().contains(tag)).count() == tags.length;
        }
    }

    @Data
    public static class IdentifiedTranslator<T> {

        private final Class<T> identifier;

        private final ITranslator<T> translator;

    }

    @Data
    public static class Gloria implements IGloria {

        private final IEngine<IGloria> moduleEngine;

        private final ISessionManager sessionManager;

        private final List<INode> commandNodes;

        private final Queue<Statement> asyncQueue;

        private final IDocument<?> configuration;

        private final String instanceIdentifier;

        private PrintStream defaultPrintStream;

        public Gloria(@NonNull final String instanceIdentifier) {
            this.instanceIdentifier = instanceIdentifier;
            GloriaAPI.instances.put(this.instanceIdentifier, this);
            this.moduleEngine = new Engine<>("gloria_modules", this);
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, IStatementProcessor>("static_pre_processors", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, ParamValidator>("parameter_validators", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, IdentifiedTranslator<?>>("translators", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, StatementPreProcessor>("pre_processors", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, Mapper>("mappers", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, IntrinsicParameterSupplier>("intrinsic_suppliers", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, IdentifiedInboundHandler>("inbound_handlers", this.moduleEngine));
            this.moduleEngine.addModularizedEntityHolder(new ModularizedEntityHolder<IGloria, ILecturer>("lecturers", this.moduleEngine));
            this.sessionManager = new SessionManager();
            this.commandNodes = new ArrayList<>();
            this.asyncQueue = new ArrayDeque<>();
            this.configuration = Document.empty();

            // TODO: 04.08.2021 Remove line if the one below does work properly
            // this.getModuleEngine().deprecated_install(DefaultModules.baseModule);

            this.getModuleEngine().register(true, DefaultModules.baseModule);
        }

        public Gloria() {
            this(UUID.randomUUID().toString());
        }

        @Override
        public INode getCommandNode(String path) {
            List<INode> nodeSet = this.commandNodes;
            final String[] shards = path.split(" ");
            for (int i = 0, shardsLength = shards.length; i < shardsLength; i++) {
                final String shard = shards[i];
                List<INode> matchingNodes = new ArrayList<>();
                if (i == 0) {
                    matchingNodes = nodeSet.stream().filter(node -> node.matches(shard)).collect(Collectors.toList());
                } else {
                    for (final INode node : nodeSet) {
                        for (INode node11 : node.getChildren()) {
                            if (node11.matches(shard)) {
                                matchingNodes.add(node11);
                            }
                        }
                    }
                }
                nodeSet = matchingNodes;
            }
            return nodeSet.size() == 0 ? null : nodeSet.get(0);
        }

        @Override
        public INode getMostMatchingNode(String path) {
            path = path + " _";
            final String[] shards = path.split("( )+");
            final StringBuilder builder = new StringBuilder();
            INode tempNode = null;
            if (shards.length == 1) {
                return this.getCommandNode(path);
            }
            for (final String shard : shards) {
                builder.append(shard).append(" ");
                final INode node = this.getCommandNode(builder.toString().trim());
                if (node != null) {
                    tempNode = node;
                } else {
                    return tempNode;
                }
            }
            return null;
        }

        @Override
        public IGloria setDefaultCommandPrintStream(@NonNull PrintStream defaultPrintStream) {
            this.defaultPrintStream = defaultPrintStream;
            return this;
        }

        // todo remove the getter from the variable itself
        @Nullable
        @Override
        public PrintStream getDefaultCommandPrintStream() {
            return this.defaultPrintStream;
        }

        // todo return any kind of completable future
        @Override
        public CompletableFuture<CommandResult> submitAsync(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData) {
            this.submit(command, sender, unorderedData, "async");
            return null;
        }

        @Override
        public CommandResult submit(@NonNull String command) {
            return this.submit(command, IGloria.DEFAULT_INBOUND_HANDLER_IDENTIFIER);
        }

        // todo add a default command sender
        @Override
        public CommandResult submit(@NonNull String command, String inboundHandler) {
            return this.submit(command, ICommandSender.def, IGloria.DEFAULT_INBOUND_HANDLER_IDENTIFIER);
        }

        @Override
        public CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, String inboundHandler) {
            return this.submit(command, sender, Document.empty(), inboundHandler);
        }

        @Override
        public CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData, String inboundHandler) {
            return this.submit(command, sender, unorderedData, inboundHandler, (statement, commandResult) -> {
            });
        }

        /**
         * todo chain command isn't working with async mode, make a callback in which you call the next chain part
         * todo move j doc to interface
         * todo improve emission system -> basic string emission & advanced data emission
         * todo write better command chain system
         * <p>
         * 1. Resolve all shards of the command (split at whitespaces and let ''-strings together)
         * 2. Go over each replace handler and let the shard be replaced if necessary
         * <p>
         * $name -> get a var called name
         * $(expression) -> execute a BeanShell expression
         *
         * @param command        The raw command
         * @param sender         The sender of the command, to check permissions and personal session data
         * @param unorderedData  A set of entries that can modify the commands behavior
         * @param inboundHandler Determines the handler
         * @param callback       A callback, that gets executed, if the commands was handled
         * @return Information about the command and it's execution
         * @see Gloria#submit(String, ICommandSender, IDocument, String) A method of submission without a callback
         */
        @Override
        public CommandResult submit(@NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData, String inboundHandler, BiConsumer<Statement, CommandResult> callback) {
            // Resolve the inbound handler
            final AtomicReference<IdentifiedInboundHandler> handler = new AtomicReference<>();
            if (inboundHandler == null) {
                inboundHandler = this.getConfiguration().getOr("default_inbound_handler", "sync");
            }
            final String finalInboundHandler = inboundHandler;
            this.getModuleEngine().<IdentifiedInboundHandler>getEntityHolder("inbound_handlers")
                    .flatMap(mapperModularizedEntityHolder -> mapperModularizedEntityHolder.normalize().stream()
                            .filter(_handler -> _handler.getType().equals(finalInboundHandler)).findFirst()).ifPresent(handler::set);
            // Lecture the command
            final AtomicReference<String> lecturedCommand = new AtomicReference<>(command);
            String finalInboundHandler1 = inboundHandler;
            this.getModuleEngine().<ILecturer>getEntityHolder("lecturers").ifPresent(holder -> holder.normalize().forEach(lecturer -> {
                final String lectured = lecturer.lecture(lecturedCommand.get(), sender, unorderedData, finalInboundHandler1);
                lecturedCommand.set(lectured);
            }));
            // Resolve Chains & execute them
            // todo set back to ;
            final AtomicReference<CommandResult> result = new AtomicReference<>(null);

            List<String> chainParts = Arrays.asList(
                    // lecturedCommand.get().split(this.getConfiguration().getOr("command_chain_separator", "&"))
                    Utilities.splitNonSubstituted(lecturedCommand.get(), this.getConfiguration().getOr("command_chain_separator", "&"), "'")
            );
            AtomicReference<List<String>> emission = new AtomicReference<>();
            chainParts = chainParts.stream().map(String::trim).collect(Collectors.toList());
            // todo refactor the whole code segment
            if (chainParts.size() > 1) {
                // Handling as a chained command, check if the selected inbound handler is capable of handling chains // todo refactor the chaining system in the future
                // It's mandatory, that the default inbound handler is capable of handling chains
                if (!handler.get().isSupportsCommandChaining()) {
                    this.getModuleEngine().<IdentifiedInboundHandler>getEntityHolder("inbound_handlers")
                            .flatMap(mapperModularizedEntityHolder -> mapperModularizedEntityHolder.normalize().stream()
                                    .filter(_handler -> _handler.getType().equals(this.getConfiguration().getOr("default_inbound_handler", "sync"))).findFirst()).ifPresent(handler::set);
                }
                // Handle the chain
                for (String chainPart : chainParts) {
                    if (emission.get() != null) {
                        final String serializedEmission = String.join(" ", emission.get());
                        // todo better emission injector -> @()|(\s*\(\s*'(.)*'\s*,\s*\d+\s*\))
                        final Matcher matcher = Pattern.compile("@").matcher(chainPart);
                        if (matcher.find()) {
                            chainPart = chainPart.replaceAll("@", serializedEmission);
                        } else {
                            chainPart = chainPart + " " + serializedEmission;
                        }
                    }
                    try {
                        // todo make save -> no base module installed handler is null
                        final CommandResult commandResult = handler.get().handle(this, chainPart, sender, unorderedData, callback);
                        result.set(commandResult);
                        if (commandResult != null) {
                            final List<String> emissions = commandResult.getStatement().getEmissions();
                            emission.set(emissions);

                            if (commandResult.getStatement().isCanceled()) {
                                // todo handle!
                                System.out.println("A part of the chain was cancelled, abort the whole rest of the chain");
                                break;
                            }
                        }
                    } catch (final Exception e) {
                        // todo handle!
                        break;
                    }
                }
            } else {
                // It's a singleton command
                final IdentifiedInboundHandler identifiedInboundHandler = handler.get();
                if (identifiedInboundHandler == null) {
                    // todo handle appropriately (also above)
                    System.out.println("No inbound handler found, aboard execution");
                    // todo implement
                    return new CommandResult(null);
                }
                final CommandResult commandResult = identifiedInboundHandler.handle(this, chainParts.get(0), sender, unorderedData, callback);
                result.set(commandResult);
                if (commandResult != null) {
                    final List<String> emissions = commandResult.getStatement().getEmissions();
                    emission.set(emissions);
                }
            }
            return result.get();
        }

        private void _registerMethodHandler(@NonNull Method method, @Nullable Object instance, @NonNull Command command) {
            method.setAccessible(true);
            final List<Annotation> executorAnnotations = new ArrayList<>(Arrays.asList(method.getAnnotations()));
            executorAnnotations.remove(command);
            executorAnnotations.removeIf(annotation -> !annotation.annotationType().isAnnotationPresent(ExecutorAnnotations.PreProcessorHook.class));
            // Serialize the methods parameters
            final List<SerializedParameter> parameters = new ArrayList<>();
            final Parameter[] methodParameters = method.getParameters();
            for (final Parameter parameter : methodParameters) {
                parameters.add(SerializedParameter.serialize(parameter));
            }

            // todo move many statements into the execute function
            final RegisteredCommandNode node = new RegisteredCommandNode(new IStatementProcessor() {
                @Override
                public void handle(@NonNull Statement statement) {
                    final Command commandInfo = statement.getNode().getCommand();
                    if (commandInfo.type() == Command.Type.UNDEFINED || commandInfo.type() == Command.Type.HANDLER) {
                        final Object[] mappedParameters = Gloria.this.map(new Object[method.getParameterCount()], statement, "default");
                        // todo inject global variables & sessions
                        // Validate all the parameters
                        if (!validateParameters(statement, mappedParameters)) {
                            statement.setCanceled(true);
                        }
                        // Execute static preprocessors
                        //noinspection CodeBlock2Expr
                        moduleEngine.<IStatementProcessor>getEntityHolder("static_pre_processors").ifPresent(holder -> {
                            holder.normalize().forEach(staticProcessor -> staticProcessor.handle(statement));
                        });
                        // Execute local preprocessors, in that case, preprocessors indicated by an annotation on the executor
                        final List<String> excludedLocalProcessors = Arrays.asList(commandInfo.passPreprocessors());
                        statement.getNode().getExecutorAnnotations().forEach(annotation -> {
                            final List<StatementPreProcessor> matchingProcessors = new ArrayList<>();
                            //noinspection CodeBlock2Expr
                            moduleEngine.<StatementPreProcessor>getEntityHolder("pre_processors").ifPresent(holder -> {
                                matchingProcessors.addAll(holder.normalize().stream().filter(processor -> {
                                    //noinspection CodeBlock2Expr
                                    return processor.getAnnotation().equals(annotation.annotationType());
                                }).collect(Collectors.toList()));
                            });
                            matchingProcessors.forEach(matchingProcessor -> {
                                if (!excludedLocalProcessors.contains(matchingProcessor.id)) {
                                    matchingProcessor.getProcessor().handle(statement);
                                }
                            });
                        });
                        // todo find more appropriate way to handle abortion -> maybe ITask.abort()
                        if (!statement.isCanceled()) {
                            final RegisteredCommandNode node = statement.getNode();

                            // todo remove debug
                            // System.out.printf(
                            //         "Raw: %s, Params: %s, Arguments: %s%n", statement.getRaw(), statement.getParameters().toSlimString(), statement.getArguments().toSlimString()
                            // );
                            this.specializedTryCatch(() -> node.getHandler().invoke(node.getHandlerInstance(), mappedParameters),
                                    SpecifiedCatcher.of(InvocationTargetException.class, throwable -> {
                                        System.out.println("Cannot call the method!");
                                        throwable.printStackTrace();
                                    }));
                        } else {
                            System.out.println("execution cancelled");
                        }
                    } else {
                        System.out.println("Node not executable, it's not a HANDLER or UNDEFINED: " + commandInfo.type());
                    }
                }
            }, command, executorAnnotations, method, instance, parameters);
            this.addCommandNode(command.path(), Node.builder().transformableCommandBody(false).id(command.literal())
                    .aliases(Arrays.asList(command.aliases())).commandBody(node).build());
        }

        @Override
        public IGloria registerMethodHandler(Method method, @Nullable Object instance) {
            if (method.isAnnotationPresent(ExecutorAnnotations.PolymorphCommand.class)) {
                // Register handler method as polymorph
                final ExecutorAnnotations.PolymorphCommand polymorphCommand = method.getAnnotation(ExecutorAnnotations.PolymorphCommand.class);
                Arrays.stream(polymorphCommand.value()).forEach(command -> {
                    this._registerMethodHandler(method, instance, command);
                });
            } else if (method.isAnnotationPresent(Command.class)) {
                // Register as singleton
                final Command command = method.getAnnotation(Command.class);
                this._registerMethodHandler(method, instance, command);
            }
            return this;
        }

        @Override
        public IGloria registerMethodsInClass(@NonNull Class<?> holder, boolean autoInstanceInvoking) {
            Object instance = null;
            // Check if a static instance is available, if so, use it as instance
            for (Field field : holder.getDeclaredFields()) {
                if (field.isAnnotationPresent(Instance.class) && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        instance = field.get(null);
                        autoInstanceInvoking = false; // todo better way of doing so
                    } catch (final IllegalAccessException e) {
                        // todo better handling
                        e.printStackTrace();
                    }
                }
            }

            if (autoInstanceInvoking) {
                final Constructor<?> constructor;
                try {
                    constructor = holder.getConstructor();
                    constructor.setAccessible(true);
                    instance = constructor.newInstance();
                } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                    return this;
                }
            }
            for (final Method method : holder.getDeclaredMethods()) {
                // todo introduce Transient annotation to pass certain methods from being registered
                this.registerMethodHandler(method, instance);
            }
            return this;
        }

        private void addCommandNode(@NonNull String path, @NonNull INode node) {
            // It's a root node, no child resolving needed
            if (path.isEmpty()) {
                // todo improve this type of selection
                // A node with a body is preferred over one, without a body
                if (this.commandNodes.stream().anyMatch(registeredNodes -> registeredNodes.matches(node))) {
                    this.getCommandNode(node.getId()).transformInto(node);
                    return;
                }
                this.commandNodes.add(node);
                return;
            }
            INode parent = this.getCommandNode(path);
            // The parent exists, so simply append node as child
            if (parent != null) {
                if (parent.getChildren().stream().anyMatch(registeredNodes -> registeredNodes.matches(node))) {
                    return;
                }
                parent.getChildren().add(node);
                return;
            }
            // A parent node doesn't exists, try to resolve the missing nodes and create them
            INode parentNode = null;
            final String[] shards = path.split(" ");
            if (shards.length == 0) {
                return;
            }
            final StringBuilder shardBuilder = new StringBuilder();
            for (int i = 0; i < shards.length; i++) {
                final String shard = shards[i];
                shardBuilder.append(shard);
                final INode newNode = new Node(true, shard, new ArrayList<>(), new ArrayList<>(), null);
                final INode theoreticallyAvailableInPath = this.getCommandNode(shardBuilder.toString());

                // fixme second level nodes get added multiple times!

                // todo remove, it doesn't work
                if (i == 0) {
                    if (theoreticallyAvailableInPath != null) {
                        parentNode = theoreticallyAvailableInPath;
                    } else {
                        this.commandNodes.add(newNode);
                        parentNode = newNode;
                    }
                } else {
                    if (theoreticallyAvailableInPath != null) {
                        parentNode = theoreticallyAvailableInPath;
                    } else {
                        parentNode.getChildren().add(newNode);
                        parentNode = newNode;
                    }
                }
                shardBuilder.append(" ");
            }
            parentNode.getChildren().add(node);
        }

        private boolean validateParameters(@NonNull Statement statement, @NonNull Object[] mappedParameters) {
            final List<SerializedParameter> serializedParameters = statement.getNode().getParameters();
            AtomicBoolean valid = new AtomicBoolean(true);
            for (int i = 0; i < mappedParameters.length; i++) {
                final Object value = mappedParameters[i];
                final SerializedParameter parameter = serializedParameters.get(i);
                //noinspection CodeBlock2Expr
                getModuleEngine().<ParamValidator>getEntityHolder("parameter_validators").ifPresentOrElse(modularizedEntityHolder -> {
                    parameter.getAnnotations().forEach(serializedAnnotation -> {
                        // Filter all the parameter validators, that are mapped onto this type of annotation
                        final List<ParamValidator> validators = modularizedEntityHolder.normalize().stream().filter(validator -> {
                            //noinspection CodeBlock2Expr
                            return validator.getTarget().equals(serializedAnnotation.getAnnotationType());
                        }).collect(Collectors.toList());
                        // Execute each validator
                        validators.forEach(validator -> {
                            if (!validator.getValidator().test(serializedAnnotation, parameter, value)) {
                                valid.set(false);
                            }
                        });
                    });
                }, () -> {
                    // The holder wasn't found, this is a serious issue
                    System.out.println("parameter_validators holder wasn't resolved!");
                    valid.set(false);
                });
            }
            return valid.get();
        }

        private Object[] map(@NonNull Object[] parameters, @NonNull Statement statement, @SuppressWarnings("SameParameterValue") String mapper) {
            final AtomicReference<Object[]> reference = new AtomicReference<>(parameters);
            //noinspection CodeBlock2Expr
            this.getModuleEngine().<Mapper>getEntityHolder("mappers").flatMap(mapperModularizedEntityHolder ->
                    mapperModularizedEntityHolder.normalize().stream().filter(mapper1 ->
                            mapper1.getName().equals(mapper)).findFirst()).ifPresent(mapper1 -> {
                reference.set(mapper1.map(this, parameters, statement));
            });
            return reference.get();
        }

        private Couple<String, IDocument<Document>> filterArguments(@NonNull String command) {
            final IDocument<Document> arguments = new Document();
            final AtomicReference<String> atomicCommand = new AtomicReference<>(command);
            /*
             * todo can this command be removed?
             * (-)+\w+(=(('.*')|([\w]*)))?
             */
            final Stream<MatchResult> argumentResultStream = Pattern.compile("(-)+\\D+(=(('.*')|([\\w]*)))?").matcher(command).results();
            argumentResultStream.forEach(result -> {
                String group = result.group();
                String argument = group;
                argument = argument.replaceFirst("(-)+", "");
                String key;
                Object val;
                if (argument.contains("=")) {
                    final String[] keyVal = argument.split("=", 2);
                    key = keyVal[0];
                    val = keyVal[1].substring(1, keyVal[1].length() - 1);
                } else {
                    key = argument;
                    val = true;
                }
                arguments.putObject(key, val);

                // todo test this code
                // todo does the error (java.util.regex.PatternSyntaxException) happens on other places as well?
                // todo add more characters to this list (all regex metas)
                // Replace potential dangling meta characters for regex replacement
                final String[] potentialDanglingMetaCharacters = new String[]{"\\+", "\\*", "\\^"};
                for (final String danglingMetaCharacter : potentialDanglingMetaCharacters) {
                    group = group.replaceAll(danglingMetaCharacter, "\\" + danglingMetaCharacter);
                }

                atomicCommand.set(atomicCommand.get().replaceAll(group, ""));
            });
            atomicCommand.set(atomicCommand.get().trim());
            return new Couple<>(atomicCommand.get(), arguments);
        }

        @Override
        public Statement deserialize(@NonNull IGloria api, @NonNull String command, @NonNull ICommandSender sender, IDocument<?> unorderedData) {
            // Filter and cleanup for arguments
            final Couple<String, IDocument<Document>> argRes = this.filterArguments(command);
            String cleanCommand = argRes.getX1();
            final IDocument<Document> arguments = argRes.getX2();
            // Resolve the nodes
            final String nodeQuery = cleanCommand.replaceAll("( )+", " ");
            final INode node = api.getMostMatchingNode(nodeQuery);
            if (node == null) {
                System.out.println(String.format("No node was found, abort deserialization: '%s'", nodeQuery));
                return null;
            }
            final RegisteredCommandNode body = node.getCommandBody();
            if (body == null) {
                System.out.println("Node body is null, abort deserialization");
                return null;
            }
            final Command commandInfo = body.getCommand();
            final AtomicReference<String> calculatedPath = new AtomicReference<>(cleanCommand);
            int pathDeepness = commandInfo.path().length() == 0 ? 0 : (int) (Stream.of(commandInfo.path().split(" ")).count());
            final int nodeDeepness = pathDeepness + 1;
            GloriaAPI.forEachIndexed(Pattern.compile("(\\w)+").matcher(calculatedPath.get()).results(), (context, i, val) -> {
                calculatedPath.set(calculatedPath.get().replaceFirst(val.group(), "").trim());
                if (i + 1 == nodeDeepness) {
                    context.setBreakAfterThis(true);
                }
            });
            final List<String> parameters = new ArrayList<>();
            Pattern.compile("('.*')|([^ ]*)").matcher(calculatedPath.get()).results().forEach(result -> {
                String group = result.group();
                if (!group.isEmpty()) {
                    if (group.startsWith("'")) {
                        group = group.replaceFirst("'", "");
                        if (group.toCharArray()[group.length() - 1] == '\'') {
                            group = group.substring(0, group.length() - 1);
                        }
                    }
                    parameters.add(group);
                }
            });
            final IDocument<Document> finalParameters = new Document();
            for (int i = 0; i < parameters.size(); i++) {
                finalParameters.putObject(String.valueOf(i), parameters.get(i));
            }

            // Find and set the statements output stream
            final AtomicReference<PrintStream> stream = new AtomicReference<>(System.out);
            final PrintStream apiStream = api.getDefaultCommandPrintStream();
            if (apiStream != null) {
                stream.set(apiStream);
            }
            unorderedData.ifPresent(IGloria.CUSTOM_COMMAND_PRINT_STREAM_IDENTIFIER, stream::set);

            // Construct and return the parsed statement
            return new Statement(this, command, sender, unorderedData, finalParameters, arguments, body, stream.get());
        }

        @Override
        public boolean setDefaultInboundHandler(@NonNull String identifier) {
            final AtomicBoolean success = new AtomicBoolean(true);
            this.getModuleEngine().<IdentifiedInboundHandler>getEntityHolder("inbound_handlers")
                    .flatMap(mapperModularizedEntityHolder -> mapperModularizedEntityHolder.normalize().stream()
                            .filter(_handler -> _handler.getType().equals(identifier)).findFirst()).ifPresent(handler -> {
                if (!handler.isSupportsCommandChaining()) {
                    success.set(false);
                } else {
                    this.getConfiguration().put("default_inbound_handler", identifier);
                }
            });
            return success.get();
        }

        @Override
        public void removeNodeIf(@NonNull Predicate<INode> predicate) {
            final TriConsumer<IGloria, Predicate<INode>, List<INode>> semiRecursiveNodeRunner = new TriConsumer<IGloria, Predicate<INode>, List<INode>>() {
                @Override
                public void accept(IGloria v1, Predicate<INode> v2, List<INode> v3) {
                    v3.removeIf(v2);
                    v3.forEach(child -> {
                        this.accept(v1, v2, child.getChildren());
                    });
                }
            };
            semiRecursiveNodeRunner.accept(this, predicate, this.commandNodes);
        }

        @Override
        public void removeNode(@NonNull String node) {
            final String[] shards = node.split("( )*");
            if (shards.length == 1) {
                // It's the root node, just remove it
                this.getCommandNodes().stream().filter(iNode -> iNode.getId().equals(node)).findFirst().ifPresent(iNode -> {
                    // todo trigger remove node
                    this.getCommandNodes().remove(iNode);
                });
            } else {
                final StringBuilder sb = new StringBuilder();
                for (String shard : shards) {
                    sb.append(shard).append(" ");
                }
                final String parent = sb.toString(), child = shards[shards.length - 1];
                final INode parentNode = this.getCommandNode(parent);
                parentNode.getChildren().stream().filter(iNode -> iNode.getId().equals(child)).findFirst().ifPresent(iNode -> {
                    // todo trigger remove node -> in removeIf !!
                    parentNode.getChildren().removeIf(childNode -> childNode.getId().equals(child));
                });
            }
            this.getCommandNode(node);
        }

        @Override
        public void start() {
            // todo modules added after calling the start method will not be booted, fix that
            // Boot all the modules
            //noinspection CodeBlock2Expr
            this.getModuleEngine().getModules().forEach(module -> {
                module.getBootloader().fire(this, module, this.getModuleEngine());
            });
        }

        @Override
        public void shutdown() {
            // Uninstall all the modules (That also means stopping all the module related threads e.g. async handlers)
            //noinspection CodeBlock2Expr
            this.getModuleEngine().getModules().forEach(module -> {
                module.getUninstaller().fire(this, module, this.getModuleEngine());
            });
        }
    }

    @Data
    @Builder
    public static class Node implements INode {

        @Builder.Default
        private final boolean transformableCommandBody = true;

        @NonNull
        private String id;

        @Singular
        private List<String> aliases;

        @Builder.Default
        private List<INode> children = new ArrayList<>();

        private RegisteredCommandNode commandBody;

        @Override
        public boolean matches(String shard) {
            if (!this.id.equals(shard)) {
                return this.aliases.contains(shard);
            }
            return true;
        }

        @Override
        public boolean matches(INode that) {
            if (this.matches(that.getId())) {
                return true;
            }
            for (final String alias : this.aliases) {
                if (that.matches(alias)) return true;
            }
            return false;
        }

        @Override
        public boolean isSkeleton() {
            return this.commandBody == null;
        }

        @Override
        public void transformInto(@NonNull INode into) {
            this.setId(into.getId());
            this.setAliases(into.getAliases());
            // todo what was there? -> smth to transform I guess
            if (this.isTransformableCommandBody()) {
                this.setCommandBody(into.getCommandBody());
            }
        }

        @Override
        public Utilities.PrintableNode toPrintableNode() {
            final Utilities.PrintableNode.PrintableNodeBuilder builder = Utilities.PrintableNode.builder().text(this.toString());
            this.getChildren().forEach(node -> {
                builder.child(node.toPrintableNode());
            });
            return builder.build();
        }

        @Override
        public String toString() {
            return "[" + this.id + ", " +
                    this.aliases.toString() + ", " +
                    (this.commandBody == null ? "n/a" : "available") + ", " +
                    (this.isSkeleton() ? "n/a" : this.getCommandBody().getCommand().ofModule()) + "]";
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SESSION FRAMEWORK
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ToString
    @RequiredArgsConstructor
    public static class Session implements ISession {

        @NonNull
        @Getter
        private final UUID identifier;

        @NonNull
        @Getter
        private final String sessionName;

        @NonNull
        @Getter
        private final Instant sessionStart;

        @Getter
        private final Instant expiringTime;

        @Getter
        private volatile IDocument<?> sessionData = new Document();

        @Override
        public ISession setSessionData(IDocument<?> sessionData) {
            this.sessionData = sessionData;
            return this;
        }

        @Override
        public void onSessionClose() {
            // todo implement
        }
    }

    public static class SessionManager implements ISessionManager {

        private final List<ISession> sessions = new ArrayList<>();

        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        @Override
        public ISession getStaticSession() {
            return ISessionManager.STATIC_SESSION;
        }

        @Override
        public Optional<ISession> getSession(@NonNull UUID identifier, @NonNull String sessionName) {
            return this.sessions.stream().filter(session -> session.getIdentifier().equals(identifier) && session.getSessionName().equals(sessionName)).findFirst();
        }

        @Override
        public ISession createSession(@NonNull UUID identifier, @NonNull String sessionName) {
            return this.createExpiringSession(identifier, sessionName, null);
        }

        @Override
        public ISession createExpiringSession(@NonNull UUID identifier, @NonNull String sessionName, Instant expirationTime) {
            if (getSession(identifier, sessionName).isPresent()) {
                // todo handle
                return null;
            }
            final ISession session = new Session(identifier, sessionName, Instant.now(), expirationTime);
            if (expirationTime != null) {
                // todo check if the expiration time is already reached (negative val for .between)
                //noinspection CodeBlock2Expr
                this.executor.schedule(() -> {
                    this.closeSession(identifier, sessionName);
                }, Duration.between(Instant.now(), expirationTime).get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
            }
            this.sessions.add(session);
            return session;
        }

        @Override
        public void closeSession(@NonNull UUID identifier, @NonNull String sessionName) {
            this.getSession(identifier, sessionName).ifPresent(session -> {
                this.sessions.remove(session);
                session.onSessionClose();
            });
        }

        @Override
        public void closeSessionsOf(@NonNull UUID identifier) {
            this.getSessionsOf(identifier).forEach(session -> this.closeSession(session.getIdentifier(), session.getSessionName()));
        }

        @Override
        public List<ISession> getSessionsOf(@NonNull UUID identifier) {
            return this.sessions.stream().filter(session -> session.getIdentifier().equals(identifier)).collect(Collectors.toList());
        }

        @Override
        public List<ISession> getSessionsWithName(@NonNull String sessionName) {
            return this.sessions.stream().filter(session -> session.getSessionName().equals(sessionName)).collect(Collectors.toList());
        }

        @Override
        public boolean containsSession(@NonNull UUID identifier, @NonNull String sessionName) {
            return this.getSession(identifier, sessionName).isPresent();
        }

        @Override
        public int countSessionsOf(@NonNull UUID identifier) {
            return this.getSessionsOf(identifier).size();
        }

        @Override
        public void shutdown() {
            this.executor.shutdownNow();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GLORIA ADDONS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @UtilityClass
    public static class GloriaCommandAddons {

        // todo implement
        @NoArgsConstructor
        public static final class ModuleAPIAddon {

            @Command(literal = "modules", type = Command.Type.JUNCTION)
            private void modules() {
            }

            @Command(path = "modules", literal = "print")
            private void print(@IntrinsicParameterAnnotations.APIStatement Statement statement, String value) {
                statement.getApi().getModuleEngine().getModules().forEach(module -> {
                    statement.getOut().printf("[%s, %s, %s]%n", module.getName(), module.getLifecycle(), module.isStator() ? "stator" : "dynamic");
                });
            }

            @Command(path = "modules", literal = "uninstall")
            private void uninstall(@IntrinsicParameterAnnotations.APIStatement Statement statement, String module) {
                System.out.println("Trying to uninstall module: " + module);
                statement.getApi().getModuleEngine().uninstall(module);
            }
        }

        @NoArgsConstructor
        public static final class UtilityAddon {

            @Command(literal = "utilities", aliases = {"utils", "util", "u"}, type = Command.Type.JUNCTION)
            private void utils() {
            }

            /**
             * todo substitution of command chains
             */
            @Command(literal = "schedule", aliases = {"later"})
            private void schedule(@IntrinsicParameterAnnotations.APIStatement Statement statement, @ParamAnnotations.DurationMeta Duration duration, @ParamAnnotations.Flow String command) {
                statement.getOut().println("Duration in seconds: " + duration.toSeconds());
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    System.out.println("Submitting command: " + command);
                    statement.getApi().submit(command, statement.getSender(), statement.getUnorderedData(), "sync");
                }, duration.toMillis(), TimeUnit.MILLISECONDS);
            }

            @Command(path = "utilities", literal = "substitute", tags = Command.EMITTER_TAG)
            private void substitute(@IntrinsicParameterAnnotations.APIStatement Statement statement, String identifier, @ParamAnnotations.Flow String sequence) {
                statement.emit(Utilities.substitute(sequence, identifier, "'"));
            }

            @Command(literal = "help", aliases = {"?"}, description = "Shows essential information about commands", sampleUsage = "help [command_name]", arguments = @Command.Arguments(
                    @Command.Argument(identifier = "d", description = "Shows more detailed information, like package names, internal data etc.")
            ))
            private void help(@IntrinsicParameterAnnotations.APIStatement Statement statement, @ParamAnnotations.Param(name = "path") @ParamAnnotations.Flow String path) {
                final PrintStream out = statement.getOut();
                if (path == null) {
                    // Display information about the api and it's usage todo implement
                    out.println("Display info about this command..");
                    return;
                }
                final INode node = statement.getApi().getCommandNode(path);
                if (node == null) {
                    out.println("No node found, type 'gloria nodes [node]', to see the available nodes");
                    return;
                }
                final RegisteredCommandNode body = node.getCommandBody();
                if (body == null) {
                    out.println("The path is a junction, there is no command-body present");
                    return;
                }
                final Command command = body.getCommand();

                final int printLatitude = 11;
                final Utilities.PrintableNode.PrintableNodeBuilder builder = Utilities.PrintableNode.builder();
                builder.text("Information about the command '" + path + "'");
                // Add basic information
                builder
                        .child(Utilities.PrintableNode.fromKeyValueText("Description", command.description(), printLatitude))
                        .child(Utilities.PrintableNode.fromKeyValueText("usage", command.sampleUsage(), printLatitude))
                        .child(Utilities.PrintableNode.fromKeyValueText("Tags", Arrays.toString(command.tags()), printLatitude))
                        .child(Utilities.PrintableNode.fromKeyValueText("Aliases", Arrays.toString(command.aliases()), printLatitude))
                        .child(Utilities.PrintableNode.fromKeyValueText("Node Type", String.valueOf(command.type()), printLatitude));
                // Add command arguments
                final Command.Argument[] arguments = command.arguments().value();
                if (arguments.length > 0) {
                    final Utilities.PrintableNode.PrintableNodeBuilder argumentNode = Utilities.PrintableNode.builder().text("Arguments");
                    for (Command.Argument argument : arguments) {
                        argumentNode.child(Utilities.PrintableNode.fromKeyValueText(argument.identifier(), argument.description(), printLatitude));
                    }
                    builder.child(argumentNode.build());
                }
                // Add command parameters
                final List<SerializedParameter> parameters = node.getCommandBody().getParameters();
                if (parameters.size() > 0) {
                    final Utilities.PrintableNode.PrintableNodeBuilder parameterNode = Utilities.PrintableNode.builder().text("Parameters");

                    parameters.forEach(parameter -> {
                        final boolean detailed = statement.getArguments().containsKey("d");
                        final SerializedAnnotation annotation = parameter.getParameterAnnotation();
                        final Utilities.PrintableNode.PrintableNodeBuilder child = Utilities.PrintableNode.builder();
                        child.text(annotation != null ? annotation.getSerializedData().getOr("name", parameter.getParameterName()) : parameter.getParameterName())
                                .child(Utilities.PrintableNode.fromKeyValueText("Type", detailed ? parameter.getParameterType().toString() : parameter.getParameterType().getSimpleName(), printLatitude))
                                .child(Utilities.PrintableNode.fromKeyValueText("Intrinsic", parameter.isIntrinsic(), printLatitude));
                        if (annotation != null) {
                            final IDocument<?> data = annotation.getSerializedData();
                            child
                                    .child(Utilities.PrintableNode.fromKeyValueText("Description", data.getOr("description", "n/a"), printLatitude));
                        }
                        parameterNode.child(child.build());
                    });
                    builder.child(parameterNode.build());
                }
                builder.build().renderAndHandle(out::println);
            }

            @Command(path = "utilities", literal = "return", aliases = {"ret", "r"}, tags = Command.EMITTER_TAG)
            private void ret(@IntrinsicParameterAnnotations.APIStatement Statement statement, String value) {
                statement.emit(value);
            }

            @ExecutorAnnotations.PolymorphCommand({
                    @Command(literal = "echo"),
                    @Command(path = "utilities", literal = "echo", aliases = {"print", "cout", "e"})
            })
            private void echo(@IntrinsicParameterAnnotations.APIStatement Statement statement, @ParamAnnotations.Flow String text) {
                statement.getOut().println(text);
            }

            @Command(path = "utilities", literal = "nodes", description = "", arguments = @Command.Arguments({
                    @Command.Argument(identifier = "c", description = "Prints all child nodes of the next level"),
                    @Command.Argument(identifier = "d", description = "Prints more details about the node")
            }))
            private void printNodes(@IntrinsicParameterAnnotations.APIStatement Statement statement, String path) {
                final PrintStream out = statement.getOut();
                final boolean displayChildNodes = statement.getArguments().getBoolOr("c", false), detailed = statement.getArguments().getBoolOr("d", false);
                Utilities.PrintableNode printableNode;
                if (path == null) {
                    final Utilities.PrintableNode.PrintableNodeBuilder builder = Utilities.PrintableNode.builder().text("root");
                    statement.getApi().getCommandNodes().forEach(child -> {
                        if (displayChildNodes) {
                            builder.child(child.toPrintableNode());
                        } else {
                            builder.child(Utilities.PrintableNode.builder().text(child.toString()).build());
                        }
                    });
                    printableNode = builder.build();
                } else {
                    printableNode = statement.getApi().getCommandNode(path).toPrintableNode();
                    if (printableNode == null) {
                        out.println("invalid path");
                        return;
                    }
                }
                // todo use renderAndHandle
                final String[] lines = printableNode.toString().split("\\n");
                for (final String line : lines) {
                    out.println(line);
                }
            }

            @Command(path = "utilities", literal = "delay", aliases = {"sleep", "wait"})
            private void delay(@ParamAnnotations.Range(min = 0) long amount, TimeUnit unit) {
                try {
                    Thread.sleep(unit.toMillis(amount));
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // todo add background loop
            @Command(path = "utilities", literal = "loop")
            private void loop(@IntrinsicParameterAnnotations.APIStatement Statement statement, @ParamAnnotations.Range(min = -1) int amount, @ParamAnnotations.Flow String command) {
                if (amount == -1) {
                    // infinite loop
                    while (!Thread.interrupted()) {
                        statement.getApi().submit(command, ICommandSender.root, Document.empty(), "sync");
                    }
                } else {
                    // finite loop
                    for (int i = 0; i < amount; i++) {
                        // todo get data from statement
                        statement.getApi().submit(Utilities.substitute(command, "&", "'"), ICommandSender.root, Document.empty(), "sync");
                    }
                }
            }
        }

        @NoArgsConstructor
        public static final class MemoryCommandSystemAddon {

            private final Map<String, IDocument<?>> propertyMetas = new HashMap<>();

            private final IDocument<?> memory = new Document().registerListenerAdapter(new IListenerAdapter<>() {
                @Override
                public void onEntryAddedEvent(Events.EntryAddedEvent<Document> event) {
                    propertyMetas.put(event.getKey(), Document.empty());
                }

                @Override
                public void onEntryRemovedEvent(Events.EntryRemovedEvent<Document> event) {
                    if (!propertyMetas.get(event.getKey()).getBoolOr("static", false)) {
                        propertyMetas.remove(event.getKey());
                        event.getContext().remove(event.getKey());
                    }
                }
            });

            @Command(literal = "memory", aliases = {"mem", "cache", "m"}, type = Command.Type.JUNCTION)
            private void memory() {
            }

            @Command(path = "memory", literal = "set", aliases = {"put", "append", "add"}, arguments = @Command.Arguments({
                    @Command.Argument(identifier = "s", description = "Defines a static value (ignored by wipe)")
            }))
            private void set(@IntrinsicParameterAnnotations.APIStatement Statement statement, String key, String value) {
                System.out.println("set " + key + " to " + value);
                this.memory.put(key, value);
                statement.getArguments().ifPresent("s", o -> {
                    // todo is that good practice? -> better create a dedicated method
                    setStator(key, true);
                });
            }

            @Command(path = "memory", literal = "get", tags = {Command.EMITTER_TAG})
            private void get(@IntrinsicParameterAnnotations.APIStatement Statement statement, String key) {
                statement.emit(this.memory.get(key));
            }

            @Command(path = "memory", literal = "copy")
            private void copy(@IntrinsicParameterAnnotations.APIStatement Statement statement, String key, @ParamAnnotations.Flow String[] newKeys) {
                final Object o = this.memory.get(key);
                for (final String newKey : newKeys) {
                    this.memory.putObject(newKey, o);
                }
            }

            @Command(path = "memory", literal = "print", arguments = @Command.Arguments({
                    @Command.Argument(identifier = "t", description = "Enable tree printing (Print a more visually readable version).")
            }))
            private void print(@IntrinsicParameterAnnotations.APIStatement Statement statement, String target) {
                if (target == null) {
                    target = "";
                }

                String finalTarget = target;
                final PrintStream out = statement.getOut();
                statement.getArguments().<Boolean>ifPresentOr("t", ignored -> {
                    // -t argument present, printing as a one-dimensional tree structure
                    // todo make same assessment between cache/meta/null
                    final Utilities.PrintableNode.PrintableNodeBuilder builder = Utilities.PrintableNode.builder();
                    builder.text("memory");
                    this.memory.forEach((s, o) -> {
                        // todo as variable
                        builder.child(Utilities.PrintableNode.builder().text(String.format("%20s : %20s", s, o.toString())).build());
                    });
                    for (String line : builder.build().toString().split("\\n")) {
                        out.println(line);
                    }
                }, ignored -> {
                    // No -t argument present, printing raw
                    if (finalTarget.equalsIgnoreCase("cache")) {
                        out.println(this.memory.toSlimString());
                    } else if (finalTarget.equalsIgnoreCase("meta")) {
                        out.println(this.propertyMetas);
                    } else {
                        final StringBuilder sb = new StringBuilder();
                        this.memory.forEach((key, val) -> {
                            sb.append(key).append("[val=").append(val.toString());
                            final IDocument<?> meta = this.propertyMetas.get(key);
                            if (meta != null) {
                                sb.append(", meta=").append(meta.toSlimString());
                            }
                            sb.append("], ");
                        });
                        String output = sb.toString();
                        if (sb.length() > 1) {
                            output = output.substring(0, output.length() - 2);
                        }
                        out.println(output);
                    }
                });
            }

            @Command(path = "memory", literal = "wipe", aliases = {"clear"})
            private void wipe() {
                System.out.println("wiping memory");
                this.memory.clear();
            }

            @Command(path = "memory", literal = "remove", aliases = {"delete", "rem"})
            private void remove(@ParamAnnotations.Flow String[] keys) {
                for (final String key : keys) {
                    this.memory.remove(key);
                }
            }

            @Command(path = "memory meta", literal = "set", sampleUsage = "memory meta set a_name a_meta sample_value")
            private void metaSet(String entry, String property, String value) {
                final IDocument<?> meta = this.propertyMetas.get(entry);
                if (meta != null) {
                    meta.put(property, value);
                }
            }

            @Command(path = "memory meta", literal = "stator", aliases = "static", sampleUsage = "memory meta set a_name a_meta sample_value")
            private void setStator(String entry, @SuppressWarnings("SameParameterValue") boolean stator) {
                final IDocument<?> meta = this.propertyMetas.get(entry);
                if (meta != null) {
                    meta.put("static", stator);
                }
            }

            @Command(path = "memory meta", literal = "remove", sampleUsage = "memory meta rem a_name a_meta")
            private void metaRemove(String entry, String property) {
                final IDocument<?> meta = this.propertyMetas.get(entry);
                if (meta != null) {
                    meta.remove(property);
                }
            }

            @Command(path = "memory", literal = "path", sampleUsage = "sets the memory base path")
            private void setPath(String path) {
                this.memory.setQueryBasePath(path);
            }
        }
    }

    @UtilityClass
    public class DefaultModules {

        public static final Module<IGloria> baseModule = Module.<IGloria>builder().name(IEngine.BASE_MODULE).stator(true).installer((IGloria api, Module<IGloria> module, IEngine<IGloria> manager) -> {
            // Calibrate the manager
            manager.calibrate(module.getName());

            // Add auto command uninstaller (if mapped to a module, that gets uninstalled)
            manager.getEventController().registerHandler(new EventAPI.Handler<>(ModuleRemovedEvent.class, (moduleRemovedEvent, iDocument) -> {
                api.removeNodeIf(predicateNode -> {
                    if (predicateNode.getCommandBody() != null) {
                        final String ofModule = predicateNode.getCommandBody().getCommand().ofModule();
                        if (!ofModule.equals(Command.GLOBAL_COMMAND_MODULE)) {
                            return ofModule.equals(moduleRemovedEvent.getModule().getName());
                        }
                        return false;
                    }
                    return false;
                });
            }));
            // Add static preprocessors
            manager.<IStatementProcessor>getEntityHolder("static_pre_processors").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder.add(statement -> {
                    // todo implement
                });
            });
            // Add parameter validators
            manager.<ParamValidator>getEntityHolder("parameter_validators").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder
                        .add(ParamValidators.flowValidator)
                        .add(ParamValidators.relayValidator)
                        .add(ParamValidators.rangeValidator)
                        .add(ParamValidators.paramValidator)
                        .add(ParamValidators.regexValidator);
            });
            // Add object parameter translators
            manager.<IdentifiedTranslator<?>>getEntityHolder("translators").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder
                        .add(new IdentifiedTranslator<>(CharSequence.class, (p, s) -> s))
                        .add(new IdentifiedTranslator<>(int.class, ITranslator.fromString(Integer::valueOf)))
                        .add(new IdentifiedTranslator<>(boolean.class, ITranslator.fromString(Boolean::valueOf)))
                        .add(new IdentifiedTranslator<>(double.class, ITranslator.fromString(Double::valueOf)))
                        .add(new IdentifiedTranslator<>(short.class, ITranslator.fromString(Short::valueOf)))
                        .add(new IdentifiedTranslator<>(long.class, ITranslator.fromString(Long::valueOf)))
                        .add(new IdentifiedTranslator<>(byte.class, ITranslator.fromString(Byte::valueOf)))
                        .add(new IdentifiedTranslator<>(float.class, ITranslator.fromString(Float::valueOf)))
                        .add(new IdentifiedTranslator<>(char.class, (p, s) -> {
                            final char[] chars = s.toCharArray();
                            return chars.length == 0 ? null : chars[0];
                        }))
                        .add(new IdentifiedTranslator<>(UUID.class, ITranslator.fromString(UUID::fromString)))
                        .add(new IdentifiedTranslator<>(Duration.class, (parameter, s) -> {
                            final Optional<SerializedAnnotation> optionalUntil = parameter.getAnnotations().stream().filter(serializedAnnotation -> serializedAnnotation.getAnnotationType().equals(ParamAnnotations.DurationMeta.class)).findFirst();
                            final AtomicReference<Duration> atomicDuration = new AtomicReference<>(null);
                            optionalUntil.ifPresentOrElse(serializedAnnotation -> {
                                final TimeUnit unit = serializedAnnotation.getSerializedData().get("value");
                                final long offset = Long.parseLong(s);
                                atomicDuration.set(Duration.of(offset, unit.toChronoUnit()));
                            }, () -> {
                                // todo implement or else
                            });
                            return atomicDuration.get();
                        }))
                        .add(Defaults.DefaultTranslators.TIMEUNIT_TRANSLATOR);
            });
            // Add annotation-based preprocessors
            manager.<StatementPreProcessor>getEntityHolder("pre_processor").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder.add(PreProcessors.deactivatedPreProcessors);
            });
            // Add intrinsic parameter suppliers
            manager.<IntrinsicParameterSupplier>getEntityHolder("intrinsic_suppliers").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder.add(new IntrinsicParameterSupplier(IntrinsicParameterAnnotations.APISession.class) {
                    @Override
                    protected Object get(@NonNull SerializedAnnotation serializedIntrinsicAnnotation, @NonNull Statement statement, SerializedExecutor executor, @NonNull SerializedParameter parameter, int position) {
                        final String sessionName = serializedIntrinsicAnnotation.getSerializedData().getString("value");
                        final boolean autoCreate = serializedIntrinsicAnnotation.getSerializedData().getBool("autoCreate");
                        final ISessionManager sessionManager = statement.getApi().getSessionManager();
                        final AtomicReference<ISession> session = new AtomicReference<>();
                        if (sessionName.equals(IntrinsicParameterAnnotations.APISession.STATIC_SESSION)) {
                            session.set(sessionManager.getStaticSession());
                        } else {
                            final UUID uuid = statement.getSender().getUniqueID();
                            sessionManager.getSession(uuid, sessionName).ifPresentOrElse(session::set, () -> {
                                if (autoCreate) {
                                    session.set(sessionManager.createSession(uuid, sessionName));
                                }
                            });
                        }
                        return session.get();
                    }
                }).add(new IntrinsicParameterSupplier(IntrinsicParameterAnnotations.APIStatement.class) {
                    @Override
                    protected Object get(@NonNull SerializedAnnotation serializedIntrinsicAnnotation, @NonNull Statement statement, SerializedExecutor executor, @NonNull SerializedParameter parameter, int position) {
                        return statement;
                    }
                }).add(new IntrinsicParameterSupplier(IntrinsicParameterAnnotations.API.class) {
                    @Override
                    protected Object get(@NonNull SerializedAnnotation serializedIntrinsicAnnotation, @NonNull Statement statement, SerializedExecutor executor, @NonNull SerializedParameter parameter, int position) {
                        return statement.getApi();
                    }
                });
            });
            // Add mappers
            manager.<Mapper>getEntityHolder("mappers").ifPresent(mapperModularizedEntityHolder -> {
                mapperModularizedEntityHolder.add(DefaultMappers.defaultMapper);
            });
            // Add inbound handler
            // todo handle in a smaller way (only one code block)
            manager.<IdentifiedInboundHandler>getEntityHolder("inbound_handlers").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder.add(new IdentifiedInboundHandler("sync", true) {
                    // todo return result
                    @Override
                    public CommandResult handle(@NonNull IGloria gloria, @NonNull String command, @NonNull ICommandSender sender, @NonNull IDocument<?> unorderedData, BiConsumer<Statement, CommandResult> callback) {
                        final Statement statement = gloria.deserialize(gloria, command, sender, unorderedData);
                        if (statement == null) {
                            // todo handle
                            System.out.println("command cannot be deserialized");
                            return null;
                        }
                        statement.getNode().getProcessor().handle(statement);
                        final CommandResult result = new CommandResult(statement);
                        callback.accept(statement, result);
                        return result;
                    }
                });
            });
            manager.reset();
        }).build();

        public static final Module<IGloria> asyncModule = Module.<IGloria>builder().name("async_module").installer((api, module, manager) -> {
            manager.calibrate(module.getName()).<IdentifiedInboundHandler>getEntityHolder("inbound_handlers").ifPresent(modularizedEntityHolder -> {
                modularizedEntityHolder.add(new IdentifiedInboundHandler("async", false) {
                    // todo return result
                    @Override
                    public CommandResult handle(@NonNull IGloria gloria, @NonNull String command, @NonNull ICommandSender sender, @NonNull IDocument<?> unorderedData, BiConsumer<Statement, CommandResult> callback) {
                        final Statement statement = gloria.deserialize(gloria, command, sender, unorderedData);
                        if (statement == null) {
                            // todo handle
                            System.out.println("command cannot be deserialized");
                            return null;
                        }
                        gloria.getAsyncQueue().add(statement);
                        synchronized (gloria.getAsyncQueue()) {
                            gloria.getAsyncQueue().notifyAll();
                        }
                        final CommandResult result = new CommandResult(statement);
                        callback.accept(statement, result);
                        return result;
                    }
                });
            });
            manager.reset();
        }).bootloader((api, module, manager) -> {
            // Create the executor service
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                process:
                synchronized (api.getAsyncQueue()) {
                    while (api.getAsyncQueue().isEmpty()) { // todo is this the way it's supposed to be? \_c.c_/
                        try {
                            final AtomicLong timeout = new AtomicLong(5000L);
                            module.getState().ifPresent("timeout", timeout::set);
                            api.getAsyncQueue().wait(timeout.get());
                        } catch (final InterruptedException e) {
                            System.out.println("Stopping async execution, due to an InterruptedException");
                            break process;
                        }
                        final Statement statement = api.getAsyncQueue().poll();
                        if (statement != null) {
                            statement.getNode().getProcessor().handle(statement);
                        }
                    }
                }
            });
            module.getState().putObject("async_executor", executor);
        }).uninstaller((api, module, manager) -> {
            // Shutdown the async executor service
            System.out.println("Shutting down async queue executor");
            module.getState().<ExecutorService>get("async_executor").shutdownNow();
        }).build();
    }

}
