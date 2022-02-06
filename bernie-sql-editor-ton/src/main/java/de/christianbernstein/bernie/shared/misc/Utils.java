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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class Utils {

    /**
     * Normalized value of white
     */
    public static final double NORMALIZED_RGB_MAXIMUM = 441.6729559300637D;
    public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>()
            .put(boolean.class, Boolean.class)
            .put(byte.class, Byte.class)
            .put(char.class, Character.class)
            .put(double.class, Double.class)
            .put(float.class, Float.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(short.class, Short.class)
            .put(void.class, Void.class)
            .build();
    @Getter
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
    @Getter
    private static final Yaml YAML = new Yaml();

    /**
     * Normalizes the images color data according to this formula:
     * Math.abs(Math.sqrt((color.getBlue() * color.getBlue()) + (color.getRed() * color.getRed()) + (color.getGreen() * color.getGreen()))) -> double
     * <p>
     * todo test method
     *
     * @param colors the images color values
     * @return a normalized form of the image rgb values
     */
    public static double[][] normalizeColorVectors(Color[][] colors) {
        if (colors.length < 1) {
            throw new IllegalStateException("image with size zero");
        }
        final double[][] normalized = new double[colors.length][colors[0].length];
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                final Color color = colors[x][y];
                if (color == null) {
                    continue;
                }
                // magnitude
                final double mag = Math.abs(Math.sqrt((color.getBlue() * color.getBlue()) + (color.getRed() * color.getRed()) + (color.getGreen() * color.getGreen())));
                normalized[x][y] = (mag / NORMALIZED_RGB_MAXIMUM) * 100;
            }
        }
        return normalized;
    }

    @NonNull
    public Stream<String> find(@NonNull Pattern pattern, @NonNull CharSequence input) {
        return findMatches(pattern, input).map(MatchResult::group);
    }

    @NonNull
    public Stream<MatchResult> findMatches(@NonNull Pattern pattern, @NonNull CharSequence input) {
        final Matcher matcher = pattern.matcher(input);

        final Spliterator<MatchResult> spliterator = new Spliterators.AbstractSpliterator<MatchResult>(
                Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            @Override
            public boolean tryAdvance(Consumer<? super MatchResult> action) {
                if (!matcher.find()) return false;
                action.accept(matcher.toMatchResult());
                return true;
            }
        };

        return StreamSupport.stream(spliterator, false);
    }

    public <T> List<T> toList(Set<T> set) {
        return new ArrayList<>(set);
    }

    public void ifTimePassed(@NonNull Instant timestamp, @NonNull Duration time, Runnable yesRun, Runnable noRun) {
        if (timestamp.plus(time).isBefore(Instant.now())) {
            if (yesRun != null) yesRun.run();
        } else {
            if (noRun != null) noRun.run();
        }
    }

    public void ifTimePassed(@NonNull Instant timestamp, @NonNull Duration time, Runnable yesRun) {
        Utils.ifTimePassed(timestamp, time, yesRun, null);
    }

    public boolean startsWithAny(@NonNull String target, @NonNull String... startWiths) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (final String start : startWiths) {
            if (target.startsWith(start)) {
                atomicBoolean.set(true);
            }
        }
        return atomicBoolean.get();
    }

    public boolean isEven(String numberRepresentation) {
        if (numberRepresentation.isEmpty()) {
            return false;
        }
        final int i = Character.getNumericValue(numberRepresentation.charAt(numberRepresentation.length() - 1));
        return (i & 1) == 0;
    }

    public URL getResource(String resource) {
        URL url;
        // Try with the Thread Contextual Loader.
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }
        // Last ditch attempt. Get the resource from the classpath.
        return ClassLoader.getSystemResource(resource);
    }

    public InputStream getResourceAsStream(String resource) {
        final URL url = getResource(resource);
        if (url == null) {
            return null;
        }
        final File file = new File(url.getFile());
        try {
            return new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * FisherYatesShuffle
     * A Function to generate a random permutation of toShuffle[]
     */
    public <T> T[] randomize(T[] toShuffle, int n) {
        final ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = n - 1; i > 0; i--) {
            int j = tlr.nextInt(i);
            T temp = toShuffle[i];
            toShuffle[i] = toShuffle[j];
            toShuffle[j] = temp;
        }
        return toShuffle;
    }

    public Duration durationMonitoredExecution(@NonNull Runnable runnable) {
        final Instant start = Instant.now();
        runnable.run();
        final Instant finish = Instant.now();
        return Duration.between(start, finish);
    }

    /**
     * @param a first column vector
     * @param b second column vector
     * @return a + b
     */
    @NotNull
    @Contract(pure = true)
    public static double[] add(@NotNull double[] a, @NotNull double[] b) {
        // Check if addable
        if (a.length != b.length) {
            throw new ArithmeticException("Unequal dimensions");
        }
        final double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    /**
     * @param a first column vector
     * @param b second column vector
     * @return a - b
     */
    @NotNull
    @Contract(pure = true)
    public static double[] subtract(@NotNull double[] a, @NotNull double[] b) {
        // Check if addable
        if (a.length != b.length) {
            throw new ArithmeticException("Unequal dimensions");
        }
        final double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] - b[i];
        }
        return c;
    }

    /**
     * @param x      a column vector
     * @param scalar a scalar
     * @return a * x
     */
    @NotNull
    @Contract(pure = true)
    public static double[] multiplyByScalar(@NotNull double[] x, double scalar) {
        final double[] c = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            c[i] = scalar * x[i];
        }
        return c;
    }

    /**
     * @param a first column vector
     * @param b second column vector
     * @return a1 * b1 + a2 * b2 + c1 * c2
     */
    @Contract(pure = true)
    @Deprecated
    public static double multiplyByVector(@NotNull double[] a, @NotNull double[] b) {
        // Check if addable
        if (a.length != b.length) {
            throw new ArithmeticException("Unequal dimensions");
        }
        double c = 0;
        for (int i = 0; i < a.length; i++) {
            c += a[i] * b[i];
        }
        return c;
    }

    public static double multiplyViaCos(double lenA, double lenB, double angle) {
        return new BigDecimal(String.valueOf(lenA))
                .multiply(new BigDecimal(String.valueOf(lenB)))
                .multiply(new BigDecimal(String.valueOf(Math.cos(Math.toRadians(angle))))).doubleValue();
    }

    /**
     * @param a first column vector
     * @param b second column vector
     * @return a X b
     */
    @NotNull
    public static double[] multiplyByVectorViaCrossProduct(@NotNull double[] a, @NotNull double[] b) {
        // Check if addable
        if (a.length != b.length) {
            throw new ArithmeticException("Unequal dimensions");
        }
        // Generate the new vectors
        final Function<double[], double[]> f = v -> {
            final double[] v2 = new double[a.length * 2 - 2];
            for (int i = 1; i < v.length * 2 - 1; i++) {
                v2[i - 1] = i < a.length ? v[i] : v[i - v.length];
            }
            return v2;
        };
        final double[] a2 = f.apply(a), b2 = f.apply(b), c = new double[a.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = (a2[i] * b2[i + 1]) - (a2[i + 1] * b2[i]);
        }
        return c;
    }

    public static void printVector(@NotNull double[] x) {
        int maxLen = 0;
        for (final double v : x) {
            maxLen = Math.max(maxLen, String.valueOf(v).length());
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            if (i == 0) {
                // ╭╮╰╯
                sb.append(String.format("╭ %" + maxLen + "s ╮\n", x[i]));
            } else if (i == x.length - 1) {
                sb.append(String.format("╰ %" + maxLen + "s ╯\n", x[i]));
            } else {
                sb.append(String.format("│ %" + maxLen + "s │\n", x[i]));
            }
        }
        System.out.println(sb);
    }

    // safe because both Long.class and long.class are of type Class<Long>
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrap(@NonNull final Class<T> c) {
        return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
    }

    @NotNull
    public static String border(@NonNull String text, char[] font, int[] margins) {
        final String[] lines = Arrays.stream(text.split("\n")).map(String::trim).toArray(String[]::new);
        final int maxLineLen = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        final StringBuilder sb = new StringBuilder();
        sb.append(font[0]).append(String.valueOf(font[1]).repeat(margins[3] + maxLineLen + margins[1])).append(font[2]).append("\n");
        sb.append(font[3]).append(" ".repeat(margins[3] + maxLineLen + margins[1])).append(font[7]).append("\n");
        for (final String line : lines) {
            sb.append(font[3]).append(" ".repeat(margins[3])).append(line).append(" ".repeat((maxLineLen - line.length()) + margins[1])).append(font[7]).append("\n");
        }
        sb.append(font[3]).append(" ".repeat(margins[3] + maxLineLen + margins[1])).append(font[7]).append("\n");
        sb.append(font[6]).append(String.valueOf(font[5]).repeat(margins[3] + maxLineLen + margins[1])).append(font[4]);
        return sb.toString();
    }

    @NotNull
    public static String border(@NonNull String text, @NotNull String font, @NotNull List<Integer> margins) {
        return border(text, font.toCharArray(), margins.stream().mapToInt(i -> i).toArray());
    }

    @NotNull
    public static String border(@NonNull String text, @NotNull List<Integer> margins) {
        return border(text, "╔═╗║╝═╚║".toCharArray(), margins.stream().mapToInt(i -> i).toArray());
    }

    @NotNull
    public static String border(@NonNull String text, int margin) {
        return border(text, "╔═╗║╝═╚║".toCharArray(), new int[]{margin, margin, margin, margin});
    }

    @NotNull
    @Contract(pure = true)
    public static String margin(@NonNull final String text, int margin) {
        final String marginT = " ".repeat(margin);
        return marginT + text + marginT;
    }

    @NotNull
    public static String abbreviateString(@NotNull String input, int maxLength) {
        if (input.length() <= maxLength)
            return input;
        else
            return input.substring(0, maxLength - 1) + "…";
    }

    public static File loadFile(@Nullable File file, @NonNull final String path) {
        if (file == null) {
            file = new File(path);
        }
        try {
            Utils.createFileIfNotExists(file);
            return file;
        } catch (final IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return file;
        }
    }

    @SuppressWarnings("all")
    public static  void createFileIfNotExists(@NonNull final File file) throws IOException, IllegalStateException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.createNewFile()) {
                throw new IllegalStateException(String.format("File '%s' cannot be created", file.toString()));
            }
        }
    }
}
