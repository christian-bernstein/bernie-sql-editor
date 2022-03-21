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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public interface IDocument<T extends IDocument<T>> extends EventHolder<T> {

    String getQueryBasePath();

    @SuppressWarnings("UnusedReturnValue")
    T setQueryBasePath(String path);

    <V extends IDocument<?>> V as();

    <V extends IDocument<?>> V as(Class<V> target);

    Map<String, Object> toMap();

    Map<String, Object> toNewMap();

    Properties toJava();

    boolean isEmpty();

    boolean containsKey(String key);

    void clear();

    Collection<String> keys();

    int size();

    void remove(String key);

    @SuppressWarnings("UnusedReturnValue")
    T forEach(BiConsumer<String, Object> consumer);

    T putIf(BooleanSupplier condition, String key, Object value);

    // todo rename to 'put'
    T putObject(String key, Object value);

    T putObjectIfAbsent(String key, Object value);

    T put(String key, String value);

    T put(String key, boolean value);

    T put(String key, Number value);

    T put(String key, Date value);

    T put(String key, UUID value);

    Object getOrDefault(String key, Object def);

    <V> V getGeneric(String key, Class<V> target, V def);

    <V> V getGeneric(String key, Class<V> target);

    <V> V get(String key);

    <V> V getOr(String key, V def);

    <V> V getOrDo(String key, Runnable runnable);

    <V> V getOrAndDo(String key, V def, Runnable runnable);

    Object getObject(String key);

    String getString(String key);

    boolean getBool(String key);

    Number getNumber(String key);

    int getInt(String key);

    double getDouble(String key);

    long getLong(String key);

    float getFloat(String key);

    byte getByte(String key);

    char getChar(String key);

    short getShort(String key);

    BigInteger getBigInteger(String key);

    BigDecimal getBigDecimal(String key);

    UUID getUUID(String key);

    Properties getProperties(String key);

    T getDocument(String key);

    Locale getLocale(String key);

    Locale getLocaleOr(String key, Locale def);

    boolean getBoolOr(String key, boolean def);

    <V> Supplier<V> getSupplier(String key);

    <V> Consumer<V> getConsumer(String key);

    Runnable getRunnable(String key);

    <V> V[] getArray(String key);

    byte[] getByteArray(String key);

    int[] getIntArray(String key);

    <V> List<V> getList(String key);

    <V, S> Map<V, S> getMap(String key);

    T ifEquals(String path, Object expectedValue, Runnable ifTrue);

    <V> WeakSupplier<T, V> createWeakReference(String key);

    IDocument<?> querySimplePath(String key, IDocument<?> newDocument);

    T querySimplePath(String key);

    IDocument<?> ensureAvailabilityOf(IDocument<?> document);

    T collect(String... keys);

    T collect(BiConsumer<T, String> missingHandler, String... keys);

    <V> Shared<T, V> share(String key);

    <V> IDocument<T> getUpcastablesOf();

    @SuppressWarnings("UnusedReturnValue")
    <V> T ifPresent(String key, Consumer<V> handler);

    <V> T ifPresentOr(String key, Consumer<V> presentHandler, Consumer<IDocument<?>> orHandler);

    String toSlimString();

    <V> V getOrSet(String key, V def);

}
