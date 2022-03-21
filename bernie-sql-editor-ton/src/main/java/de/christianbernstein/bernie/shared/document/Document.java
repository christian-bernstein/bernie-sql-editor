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

package de.christianbernstein.bernie.shared.document;

import com.google.gson.Gson;
import de.christianbernstein.bernie.shared.misc.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("DuplicatedCode")
@Data
public class Document implements IDocument<Document> {

    @Getter
    private transient final List<IListenerAdapter<Document>> listenerAdapters = new ArrayList<>();

    private transient String queryBase;

    @NonNull
    private volatile Map<String, Object> data = new ConcurrentHashMap<>();

    private transient boolean listenerActiveState = true;

    @SuppressWarnings("unused")
    public static Document empty() {
        return new Document();
    }

    @SuppressWarnings("unused")
    public static Document of(Map<String, Object> map) {
        final Document document = new Document() {
        };
        map.forEach(document::putObject);
        return document;
    }

    @SuppressWarnings("unused")
    public static Document of(String k1, Object v1) {
        return of(Map.of(k1, v1));
    }

    @SuppressWarnings("unused")
    public static Document of(Object... params) {
        if ((params.length % 2) == 0) {
            final Document document = new Document() {
            };
            for (int i = 0; i < params.length; i += 2) {
                document.putObject(params[i].toString(), params[i + 1]);
            }
            return document;
        } else {
            throw new IllegalStateException("Number of parameters must be even, because otherwise the key-value policy will break");
        }
    }

    @SuppressWarnings("unused")
    public static Document fromProperties(Properties... properties) {
        final Document document = new Document();
        for (final Properties property : properties) {
            property.forEach((o, o2) -> document.putObject(o.toString(), o2));
        }
        return document;
    }

    @SuppressWarnings("unused")
    public static Document fromArguments(String... args) {
        return Document.fromArgumentsArray(args);
    }

    public static Document fromArgumentsArray(String[] args) {
        final Document document = new Document();
        for (String arg : args) {
            arg = arg.replaceFirst("(-)*", "");
            if (arg.contains("=")) {
                final String[] split = arg.split("=");
                document.putObject(split[0], split[1]);
            } else {
                document.put(arg, true);
            }
        }
        return document;
    }

    @Override
    public @NonNull Document putObject(String key, Object value) {
        final Object oldValue = this.data.get(key);
        this.fire(new Events.EntryAddedEvent<>(this, key, value));
        this.data.put(this.queryBase != null ? this.queryBase + "." + key : key, value);
        return this;
    }

    @Override
    public Object getOrDefault(@NonNull String key, Object def) {
        final Object o = this.data.get(this.queryBase != null ? this.queryBase + "." + key : key);
        if (o == null) {
            return def;
        }
        return o;
    }

    public <T> T getFromMapToObjectViaJson(@NonNull final String key, @NonNull Class<T> type) {
        final Map<Object, Object> map = this.getMap(key);
        if (map != null) {
            final Gson gson = Utils.getGSON();
            final String json = gson.toJson(map);
            return gson.fromJson(json, type);
        } return null;
    }

    public <T> T getFromMapToObjectViaJson(@NonNull final String key, @NonNull Type type) {
        final Map<Object, Object> map = this.getMap(key);
        if (map != null) {
            final Gson gson = Utils.getGSON();
            final String json = gson.toJson(map);
            return gson.fromJson(json, type);
        } return null;
    }

    @Override
    public String getQueryBasePath() {
        return this.queryBase;
    }

    @Override
    public Document setQueryBasePath(String path) {
        this.queryBase = path;
        return this;
    }

    @Override
    public <T extends IDocument<?>> T as() {
        try {
            @SuppressWarnings("unchecked") final T t = (T) this;
            return t;
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T extends IDocument<?>> T as(Class<T> target) {
        try {
            @SuppressWarnings("unchecked") final T t = (T) this;
            return t;
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public @NonNull Map<String, Object> toNewMap() {
        return new HashMap<>(this.toMap());
    }

    @Override
    public @NonNull Properties toJava() {
        final Properties properties = new Properties();
        properties.putAll(this.toMap());
        return properties;
    }

    @Override
    public boolean isEmpty() {
        return this.toMap().isEmpty();
    }

    @Override
    public boolean containsKey(@NonNull String key) {
        return this.getObject(key) != null;
    }

    @Override
    public void clear() {
        this.data.keySet().forEach(this::remove);
    }

    @Override
    public @NonNull Collection<String> keys() {
        return this.data.keySet();
    }

    @Override
    public int size() {
        return this.data.size();
    }

    @Override
    public void remove(@NonNull String key) {
        this.fire(new Events.EntryRemovedEvent<>(this, key, this.get(key)));
        this.data.remove(key);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Override
    public @NonNull Document forEach(@NonNull BiConsumer<String, Object> consumer) {
        this.toMap().forEach(consumer);
        return this;
    }

    @Override
    public Document putIf(BooleanSupplier condition, String key, Object value) {
        if (condition.getAsBoolean()) {
            this.putObject(key, value);
        }
        return this;
    }

    @Override
    public @NonNull Document putObjectIfAbsent(@NonNull String key, Object value) {
        if (!this.containsKey(key)) {
            this.putObject(key, value);
        }
        return this;
    }

    @Override
    public @NonNull Document put(@NonNull String key, String value) {
        return this.putObject(key, value);
    }

    @Override
    public @NonNull Document put(@NonNull String key, boolean value) {
        return this.putObject(key, value);
    }

    @Override
    public @NonNull Document put(@NonNull String key, Number value) {
        return this.putObject(key, value);
    }

    @Override
    public @NonNull Document put(@NonNull String key, Date value) {
        return this.putObject(key, value);
    }

    @Override
    public @NonNull Document put(@NonNull String key, UUID value) {
        return this.putObject(key, value);
    }

    @Override
    public <T> T getGeneric(@NonNull String key, @NonNull Class<T> target, T def) {
        final Object obj = this.getObject(key);
        if (obj == null) {
            return def;
        }
        try {
            return target.cast(obj);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> T getGeneric(@NonNull String key, @NonNull Class<T> target) {
        return getGeneric(key, target, null);
    }

    @Override
    public <T> T get(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final T t = (T) this.getObject(key);
            return t;
        } catch (final ClassCastException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public <V> V getOr(String key, V def) {
        final V v = this.get(key);
        return v == null ? def : v;
    }

    @Override
    public <V> V getOrDo(String key, Runnable runnable) {
        if (!this.containsKey(key)) {
            runnable.run();
        }
        return this.getOr(key, null);
    }

    @Override
    public <V> V getOrAndDo(String key, V def, Runnable runnable) {
        if (!this.containsKey(key)) {
            runnable.run();
            return def;
        }
        return this.getOr(key, null);
    }

    @Override
    public Object getObject(@NonNull String key) {
        return this.getOrDefault(key, null);
    }

    @Override
    public String getString(@NonNull String key) {
        return String.valueOf(this.getObject(key));
    }

    @Override
    public boolean getBool(@NonNull String key) {
        return Boolean.parseBoolean(this.getString(key));
    }

    @Override
    public Number getNumber(@NonNull String key) {
        return Double.valueOf(this.getString(key));
    }

    @Override
    public int getInt(@NonNull String key) {
        return Integer.parseInt(this.getString(key));
    }

    @Override
    public double getDouble(@NonNull String key) {
        return Double.parseDouble(this.getString(key));
    }

    @Override
    public long getLong(@NonNull String key) {
        return Long.parseLong(this.getString(key));
    }

    @Override
    public float getFloat(@NonNull String key) {
        return Float.parseFloat(this.getString(key));
    }

    @Override
    public byte getByte(@NonNull String key) {
        return get(key);
    }

    @Override
    public char getChar(@NonNull String key) {
        return get(key);
    }

    @Override
    public short getShort(@NonNull String key) {
        return Short.parseShort(this.getString(key));
    }

    @Override
    public BigInteger getBigInteger(@NonNull String key) {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(@NonNull String key) {
        return null;
    }

    @Override
    public UUID getUUID(@NonNull String key) {
        return UUID.fromString(this.getString(key));
    }

    @Override
    public Properties getProperties(@NonNull String key) {
        return this.get(key);
    }

    @Override
    public Document getDocument(@NonNull String key) {
        return this.get(key);
    }

    @Override
    public Locale getLocale(@NonNull String key) {
        return this.get(key);
    }

    @Override
    public Locale getLocaleOr(@NonNull String key, Locale def) {
        final Locale locale = this.getLocale(key);
        return locale == null ? def : locale;
    }

    @Override
    public boolean getBoolOr(@NonNull String key, boolean def) {
        final String s = this.getString(key);
        if (s == null) return def;
        return Boolean.parseBoolean(s);
    }

    @Override
    public <T> Supplier<T> getSupplier(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final Supplier<T> supplier = (Supplier<T>) this.getObject(key);
            return supplier;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> Consumer<T> getConsumer(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final Consumer<T> consumer = (Consumer<T>) this.getObject(key);
            return consumer;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Runnable getRunnable(@NonNull String key) {
        try {
            return (Runnable) this.getObject(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> T[] getArray(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final T[] t = (T[]) this.getObject(key);
            return t;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public byte[] getByteArray(@NonNull String key) {
        return (byte[]) this.getObject(key);
    }

    @Override
    public int[] getIntArray(@NonNull String key) {
        return (int[]) this.getObject(key);
    }

    @Override
    public <T> List<T> getList(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final List<T> list = (List<T>) this.getObject(key);
            return list;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T, V> Map<T, V> getMap(@NonNull String key) {
        try {
            @SuppressWarnings("unchecked") final Map<T, V> map = (Map<T, V>) this.getObject(key);
            return map;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Document ifEquals(String path, Object expectedValue, Runnable ifTrue) {
        if (this.get(path).equals(expectedValue)) ifTrue.run();
        return this;
    }

    @Override
    public <T> WeakSupplier<Document, T> createWeakReference(@NonNull String key) {
        return WeakSupplier.of(this, document -> document.get(key));
    }

    @Override
    public @NonNull IDocument<?> querySimplePath(@NonNull String key, @NonNull IDocument<?> newDocument) {
        final Map<String, Object> found = new HashMap<>();
        this.toMap().forEach((s, o) -> {
            if (s.startsWith(key)) {
                found.put(s, o);
            }
        });
        found.forEach(newDocument::putObject);
        return newDocument;
    }

    @Override
    public @NonNull Document querySimplePath(@NonNull String key) {
        return (Document) this.querySimplePath(key, new Document());
    }

    @Override
    public IDocument<?> ensureAvailabilityOf(IDocument<?> document) {
        document.forEach((key, value) -> {
            if (!this.containsKey(key)) {
                this.putObject(key, value);
            }
        });
        return this;
    }

    @Override
    public Document collect(String... keys) {
        return this.collect(null, keys);
    }

    @Override
    public Document collect(BiConsumer<Document, String> missingHandler, String... keys) {
        final Document document = new Document();
        for (final String key : keys) {
            final Object object = this.getObject(key);
            document.putObject(key, object);
            if (object == null && missingHandler != null) missingHandler.accept(document, key);
        }
        return document;
    }

    @Override
    public <T> Shared<Document, T> share(@NonNull String key) {
        return Shared.of(this, (document) -> document.get(key), (document, t) -> document.putObject(key, t));
    }

    // todo add to base interface methods
    public <T> Shared<Document, T> share(@NonNull String key, T def) {
        this.putObjectIfAbsent(key, def);
        return Shared.of(this, (document) -> document.get(key), (document, t) -> document.putObject(key, t));
    }

    @Override
    public <V> IDocument<Document> getUpcastablesOf() {
        final IDocument<Document> newDocument = new Document();
        this.forEach((s, o) -> {
            try {
                @SuppressWarnings("unchecked") final V v = (V) o;
                newDocument.putObject(s, o);
            } catch (final ClassCastException ignored) {
            }
        });
        return null;
    }

    @Override
    public <V> Document ifPresent(String key, Consumer<V> handler) {
        return this.ifPresentOr(key, handler, null);
    }

    @Override
    public <V> Document ifPresentOr(String key, Consumer<V> presentHandler, Consumer<IDocument<?>> orHandler) {
        final V v = this.get(key);
        if (v != null) {
            presentHandler.accept(v);
        } else {
            if (orHandler != null) {
                orHandler.accept(this);
            }
        }
        return this;
    }

    @Override
    public @NonNull Map<String, Object> toMap() {
        return this.data;
    }

    @Override
    public @NonNull String toSlimString() {
        return String.format("%s", this.data);
    }

    @Override
    public String toString() {
        return this.toSlimString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getOrSet(String key, V def) {
        final Object val = this.get(key);
        if (val == null) {
            this.putObject(key, def);
            return def;
        }
        return (V) val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;
        final Document document = (Document) o;
        return Objects.equals(getData(), document.getData());
    }

    public Document copy() {
        return Document.of(this.getData());
    }

    @Override
    public IDocument<?> registerListenerAdapter(IListenerAdapter<Document> adapter) {
        this.listenerAdapters.add(adapter);
        return this;
    }

    @Override
    public IDocument<?> clearListeners() {
        // todo bulk delete event
        this.listenerAdapters.clear();
        return this;
    }

    @Override
    public IDocument<?> setListenerActiveState(boolean active) {
        this.listenerActiveState = active;
        return this;
    }

    @Override
    public <V extends Event<Document>> void fire(V event) {
        if (!this.listenerActiveState) {
            return;
        }
        final Map<IListenerAdapter<Document>, List<Method>> handlers = new HashMap<>();
        this.listenerAdapters.forEach(documentIListenerAdapter -> {
            final List<Method> methods = Arrays.stream(documentIListenerAdapter.getClass().getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 1 && method.getParameters()[0].getType().equals(event.getClass()))
                    .collect(Collectors.toList());
            handlers.put(documentIListenerAdapter, methods);
        });

        handlers.forEach((adapter, methods) -> methods.forEach(method -> {
            try {
                method.setAccessible(true);
                method.invoke(adapter, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }));
    }
}
