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
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
@Builder
public class ConsoleLogger {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final Map<String, Function<ConsoleLogger, String>> replacers = new HashMap<>();

    private final int dateAnnotationWidth = 23, moduleAnnotationWidth = 18;

    public static boolean init = false;

    public static ConsoleLogger def() {
        return ConsoleLogger.builder().build();
    }

    public static void main(String[] args) {
        System.out.println("       ___\n" +
                "  ____/ (___ " + ANSI_PURPLE + "Bernie-7491" + ANSI_RESET + " _   _____  _______  __  © 2021 Christian Bernstein\n" +
                " / __  / / ___/ ___/ __ | | / / _ \\/ ___/ / / /  \n" +
                "/ /_/ / (__  / /__/ /_/ | |/ /  __/ /  / /_/ /   [ver: 1.24]\n" +
                "\\__,_/_/____/\\___/\\____/|___/\\___/_/   \\__, /    [release: 10.2021]\n" +
                "                                      /____/\n\n");
    }

    @NonNull
    public ConsoleLogger log(@NonNull LogType type, @NonNull String module, @NonNull IDocument<?> document, @NonNull Object... message) {
        log(type, module, Arrays.stream(message).map(Objects::toString).collect(Collectors.joining(", ")));
        return this;
    }

    @NonNull
    public ConsoleLogger log(@NonNull LogType type, @NonNull Object... message) {
        this.log(type, "", Document.empty(), message);
        return this;
    }

    @Contract("_, _ -> this")
    private ConsoleLogger replace(@NonNull String text, @NotNull @NonNull Object... replacers) {
        if (replacers.length % 2 != 0) {
            System.err.println("% 2 != 0");
        } else {
            final Map<String, Object> replacerMap = new HashMap<>();
            for (int i = 0; i < replacers.length; i += 2) {
                replacerMap.put(replacers[i].toString(), replacers[i + 1]);
            }
            for (final Map.Entry<String, Object> entry : replacerMap.entrySet()) {
                final String s = entry.getKey();
                final Object o = entry.getValue();
                text = text.replaceAll(String.format("$%s", s), o.toString());
            }
            for (final Map.Entry<String, Function<ConsoleLogger, String>> entry : this.replacers.entrySet()) {
                final String s = entry.getKey();
                final Function<ConsoleLogger, String> loggerStringFunction = entry.getValue();
                text = text.replaceAll(String.format("$%s", s), loggerStringFunction.apply(this));
            }
            // todo implement
        }
        return this;
    }

    @Getter
    public enum LogType {

        INFO(ANSI_PURPLE, ANSI_PURPLE, "i"),
        DEBUG(ANSI_BLUE, "", "d"),
        WARN(ConsoleColors.YELLOW_BACKGROUND_BRIGHT,ConsoleColors.YELLOW, "⚠"),
        ERROR(ConsoleColors.RED_BACKGROUND_BRIGHT, ConsoleColors.RED_BRIGHT, "!"),
        SUCCESS(ConsoleColors.GREEN_BACKGROUND, "", "+");

        private final String prefixColor;

        private final String color;

        private final String prefix;

        LogType(String prefixColor, String color, String prefix) {
            this.prefixColor = prefixColor;
            this.color = color;
            this.prefix = prefix;
        }
    }

    public static void createSeparator(int dateWith, int modWith) {
        final StringBuilder sb = new StringBuilder()
                .append("─".repeat(4))
                .append("┼")
                .append("─".repeat(dateWith + 2))
                .append("┼")
                .append("─".repeat(modWith + 2))
                .append("┼")
                .append("─".repeat(300));
        System.out.println(sb);
    }

    public void log(LogType type, String module, String message) {
        final StringBuilder sb = new StringBuilder()
                .append("[")
                .append(type.prefixColor)
                .append(" ")
                .append(type.prefix)
                .append(" ")
                .append(ANSI_RESET)
                .append("] ")
                .append("│")
                .append(Utils.margin(String.format("%"+ this.dateAnnotationWidth + "s", Utils.abbreviateString(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS").format(new Date()), this.dateAnnotationWidth)), 1))
                .append("│")
                .append(Utils.margin(String.format("%"+ this.moduleAnnotationWidth + "s", Utils.abbreviateString(module, this.moduleAnnotationWidth)), 1))
                .append("│ ");

        switch (type) {
            case WARN, ERROR, SUCCESS -> sb.append(type.color);
        }
        sb.append(message).append(ANSI_RESET);

        if (!init) {
            init = true;
            ConsoleLogger.init(this.dateAnnotationWidth, this.moduleAnnotationWidth);
        }

        System.out.println(sb);
    }

    public static void init(int dateWith, int modWith) {
        final StringBuilder sb = new StringBuilder()
                .append(" ".repeat(6))
                .append("╷")
                .append(Utils.margin(String.format("\033[0;1m%"+ dateWith + "s", "Date".toUpperCase(Locale.ROOT)), 1))
                .append("╷")
                .append(Utils.margin(String.format("\033[0;1m%"+ modWith + "s", "Module".toUpperCase(Locale.ROOT)), 1))
                .append("╷")
                .append(ANSI_RESET);
        System.out.println(sb);
    }
}
